import ReverseCallAdapter
import Integration
import Testing

@Test @MainActor
func actorOwnedReverseCallPreservesCachedPayloadIdentity() async throws {
    let original = Payload(label: "retained on original actor")
    let stream = AsyncStream<ReverseNotice>.makeStream()
    let request = MainActorReverseRequest(argument: original, operation: { value in
        await Task.yield()
        return value
    }, notify: { notice in _ = stream.continuation.yield(notice) })
    let ticket = try request.start()
    await ticket.waitForOperation()
    var iterator = stream.stream.makeAsyncIterator()
    #expect(await iterator.next() == .ready)
    let returned = try request.take()
    #expect(returned === original)
    #expect(original.label == "retained on original actor")
    #expect(ticket.isClosed)
    do {
        _ = try request.take()
        Issue.record("Second take unexpectedly succeeded")
    } catch {
        #expect(error as? ReverseCallFailure == .noResult)
    }
    stream.continuation.finish()
}

@Test @MainActor
func cancellationBeforeDispatchDoesNotInvokeOperation() async throws {
    let original = Payload(label: "cancel before dispatch")
    var called = false
    let request = MainActorReverseRequest(argument: original, operation: { value in
        called = true
        return value
    }, notify: { _ in })
    let ticket = try request.start()
    ticket.cancel()
    await ticket.waitForOperation()
    #expect(!called)
    #expect(ticket.isCancelled)
    do {
        _ = try request.take()
        Issue.record("Cancelled request exposed a result")
    } catch {
        #expect(error is CancellationError)
    }
    #expect(original.label == "cancel before dispatch")
}

private enum NotificationError: Error { case rejected }

@Test @MainActor
func failedReadyNotificationDiscardsTypedResult() async throws {
    let original = Payload(label: "notification failure")
    let request = MainActorReverseRequest(argument: original, operation: { $0 },
        notify: { notice in if notice == .ready { throw NotificationError.rejected } })
    let ticket = try request.start()
    await ticket.waitForOperation()
    #expect(ticket.isClosed)
    do {
        _ = try request.take()
        Issue.record("Failed notification exposed a result")
    } catch {
        guard let failure = error as? ReverseCallFailure, case .notificationFailed = failure else {
            Issue.record("Expected notification failure, got \(error)")
            return
        }
    }
    #expect(original.label == "notification failure")
}

private actor SuspensionGate {
    private var open = false
    private var continuation: CheckedContinuation<Void, Never>?
    func wait() async {
        if open { return }
        await withCheckedContinuation { continuation = $0 }
    }
    func release() {
        open = true
        continuation?.resume()
        continuation = nil
    }
}

@Test @MainActor
func cancellationDuringSuspensionWaitsForActualOperationCompletion() async throws {
    let original = Payload(label: "suspended operation")
    let gate = SuspensionGate()
    let started = AsyncStream<Bool>.makeStream()
    let notices = AsyncStream<ReverseNotice>.makeStream()
    var completed = false
    let request = MainActorReverseRequest(argument: original, operation: { value in
        defer { completed = true }
        _ = started.continuation.yield(true)
        await gate.wait()
        return value
    }, notify: { _ = notices.continuation.yield($0) })
    let ticket = try request.start()
    var starts = started.stream.makeAsyncIterator()
    #expect(await starts.next() == true)
    ticket.cancel()
    #expect(!completed)
    await gate.release()
    await ticket.waitForOperation()
    #expect(completed)
    var events = notices.stream.makeAsyncIterator()
    #expect(await events.next() == .cancelled)
    do {
        _ = try request.take()
        Issue.record("Suspended cancelled request exposed a result")
    } catch { #expect(error is CancellationError) }
    #expect(original.label == "suspended operation")
    started.continuation.finish()
    notices.continuation.finish()
}

@Test @MainActor
func cancellationAfterReadinessDiscardsTheUntakenResult() async throws {
    let original = Payload(label: "cancel after ready")
    let notices = AsyncStream<ReverseNotice>.makeStream()
    let request = MainActorReverseRequest(argument: original, operation: { $0 },
        notify: { _ = notices.continuation.yield($0) })
    let ticket = try request.start()
    await ticket.waitForOperation()
    var events = notices.stream.makeAsyncIterator()
    #expect(await events.next() == .ready)
    ticket.cancel()
    // Await the actor cleanup notice, rather than treating waitForOperation as cleanup.
    #expect(await events.next() == .cancelled)
    do {
        _ = try request.take()
        Issue.record("Cancellation after ready exposed the result")
    } catch { #expect(error is CancellationError) }
    request.discard()
    request.discard()
    #expect(ticket.isClosed)
    #expect(original.label == "cancel after ready")
    notices.continuation.finish()
}

@Test @MainActor
func finishedTaskDoesNotRetainItsRequestFrame() async throws {
    let original = Payload(label: "frame lifetime")
    var owner: MainActorReverseRequest<Payload, Payload>? =
        MainActorReverseRequest(argument: original, operation: { $0 }, notify: { _ in })
    weak let observed = owner
    let ticket = try #require(owner).start()
    await ticket.waitForOperation()
    let returned = try #require(owner).take()
    #expect(returned === original)
    owner = nil
    for _ in 0..<100 {
        if observed == nil { break }
        await Task.yield()
    }
    #expect(observed == nil)
    #expect(original.label == "frame lifetime")
}
