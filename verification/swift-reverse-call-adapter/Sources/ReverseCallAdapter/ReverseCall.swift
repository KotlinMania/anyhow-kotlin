import Foundation
import Synchronization

// This dispatch surface carries no Kotlin object or raw pointer across isolation.
public typealias ActorBoundReverseWork = @Sendable @isolated(any) () async -> Void

public enum ReverseNotice: Sendable, Equatable {
    case ready
    case cancelled
    case failed(String)
}

public enum ReverseCallFailure: Error, Sendable, Equatable {
    case alreadyStarted
    case noResult
    case notificationFailed(String)
    case operationFailed(String)
}

// Checked Sendable control state contains only Sendable task handles/closures and flags.
public final class ReverseCallTicket: Sendable {
    private struct State {
        var task: Task<Void, Never>?
        var cleanup: ActorBoundReverseWork?
        var cancelled = false
        var closed = false
    }
    private let state = Mutex(State())

    fileprivate func install(_ task: Task<Void, Never>, cleanup: @escaping ActorBoundReverseWork) {
        let cancel = state.withLock { state in
            state.task = task
            state.cleanup = cleanup
            return state.cancelled
        }
        if cancel { task.cancel() }
    }

    public func cancel() {
        let action = state.withLock { state -> (Task<Void, Never>?, ActorBoundReverseWork?) in
            guard !state.closed, !state.cancelled else { return (nil, nil) }
            state.cancelled = true
            return (state.task, state.cleanup)
        }
        action.0?.cancel()
        if let cleanup = action.1 {
            Task { await cleanup() }
        }
    }

    public var isCancelled: Bool { state.withLock { $0.cancelled } }
    public var isClosed: Bool { state.withLock { $0.closed } }

    public func waitForOperation() async {
        let task = state.withLock { $0.task }
        await task?.value
    }

    fileprivate func operationFinished() {
        state.withLock { $0.task = nil }
    }

    fileprivate func close() {
        state.withLock { state in
            state.closed = true
            state.cleanup = nil
        }
    }
}

// Concrete implementation in a known lexical isolation domain. A caller-side generator
// must emit the equivalent code in the actual callback's actor scope. This is NOT an
// implicit conversion from an arbitrary opaque @isolated(any) (Argument) -> Value.
@MainActor
public final class MainActorReverseRequest<Argument, Value> {
    public typealias Operation = @MainActor (Argument) async throws -> Value
    private enum Phase: Equatable { case created, running, ready, taken, cancelled, failed }
    private enum Slot { case empty, value(Value) }
    private var phase = Phase.created
    private var argument: Argument?
    private var operation: Operation?
    private var result = Slot.empty
    private var failure: ReverseCallFailure?
    private let notify: @Sendable (ReverseNotice) throws -> Void
    private let ticket = ReverseCallTicket()

    public init(argument: Argument, operation: @escaping Operation,
                notify: @escaping @Sendable (ReverseNotice) throws -> Void) {
        self.argument = argument
        self.operation = operation
        self.notify = notify
    }

    public func start() throws -> ReverseCallTicket {
        guard phase == .created else { throw ReverseCallFailure.alreadyStarted }
        phase = .running
        // Both closures are formed in the same lexical actor scope as the object state.
        let work: ActorBoundReverseWork = { @MainActor [self] in await execute() }
        let cleanup: ActorBoundReverseWork = { @MainActor [weak self] in self?.cancelOnActor() }
        let task = Task { await work() }
        ticket.install(task, cleanup: cleanup)
        return ticket
    }

    private func execute() async {
        defer {
            self.argument = nil
            self.operation = nil
            ticket.operationFinished()
        }
        guard !ticket.isCancelled else { cancelOnActor(); return }
        guard let argument, let operation else {
            fail(.noResult)
            return
        }
        do {
            // No sending value/foreign callback object crosses this actor boundary.
            let value = try await operation(argument)
            guard !ticket.isCancelled, !Task.isCancelled else { cancelOnActor(); return }
            result = .value(value)
            phase = .ready
            do {
                try notify(.ready)
            } catch {
                result = .empty
                fail(.notificationFailed(String(describing: error)))
            }
        } catch {
            if ticket.isCancelled || error is CancellationError {
                cancelOnActor()
            } else {
                fail(.operationFailed(String(describing: error)))
            }
        }
    }

    // Typed take runs on the owner actor. C/native result conversion must be generated
    // at this lexical point, not by sending a cached KotlinBase through another task.
    public func take() throws -> Value {
        if ticket.isCancelled || phase == .cancelled { throw CancellationError() }
        if let failure { throw failure }
        guard phase == .ready, case let .value(value) = result else {
            throw ReverseCallFailure.noResult
        }
        result = .empty
        phase = .taken
        ticket.close()
        return value
    }

    public func discard() {
        result = .empty
        argument = nil
        operation = nil
        // Cancel a running operation before closing its control handle.
        ticket.cancel()
        phase = .cancelled
        ticket.close()
    }

    private func cancelOnActor() {
        guard phase == .running || phase == .ready else { return }
        result = .empty
        phase = .cancelled
        try? notify(.cancelled)
        ticket.close()
    }

    private func fail(_ error: ReverseCallFailure) {
        result = .empty
        phase = .failed
        failure = error
        try? notify(.failed(String(describing: error)))
        ticket.close()
    }
}
