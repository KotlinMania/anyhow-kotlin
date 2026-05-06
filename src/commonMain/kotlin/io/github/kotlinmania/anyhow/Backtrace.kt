// port-lint: source src/backtrace.rs
package io.github.kotlinmania.anyhow

/**
 * Kotlin translation of the upstream `backtrace.rs`.
 *
 * The Rust upstream conditionally uses either:
 *
 * - `std::backtrace::Backtrace`,
 * - the external `backtrace` crate (when the `backtrace` feature is enabled),
 * - or no backtrace support.
 *
 * Kotlin does not have an equivalent feature matrix. This port models backtraces using Kotlin's
 * [StackTraceElement] arrays captured from a [Throwable].
 */

public class Backtrace private constructor(
    private val captured: Array<StackTraceElement>,
) {
    public fun status(): BacktraceStatus = BacktraceStatus.Captured

    override fun toString(): String = buildString {
        append("Stack backtrace:\n")
        for ((i, frame) in captured.withIndex()) {
            append("   ")
            append(i)
            append(": ")
            append(frame.toString())
            append('\n')
        }
    }.trimEnd()

    public companion object {
        public fun capture(): Backtrace = Backtrace(Throwable().stackTrace)
    }
}

public enum class BacktraceStatus {
    Unsupported,
    Disabled,
    Captured,
}

/**
 * Kotlin analog of the upstream `backtrace!()` macro.
 */
internal fun backtrace(): Backtrace? = Backtrace.capture()

/**
 * Kotlin analog of the upstream `backtrace_if_absent!($err)` macro.
 *
 * In Rust, this avoids capturing an additional backtrace when the underlying error type already
 * provides one through the nightly `Error::provide` mechanism. Kotlin errors always have a stack
 * trace, so this function only captures a new backtrace if the provided throwable has an empty
 * stack trace.
 */
internal fun backtraceIfAbsent(err: Throwable): Backtrace? {
    return if (err.stackTrace.isEmpty()) backtrace() else null
}

internal fun backtraceIfAbsent(err: StdError): Backtrace? {
    return when (err) {
        is Throwable -> backtraceIfAbsent(err)
        else -> backtrace()
    }
}

