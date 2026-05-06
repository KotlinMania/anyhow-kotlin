// port-lint: source src/context.rs
package io.github.kotlinmania.anyhow

/**
 * Kotlin translation of the upstream `context.rs`.
 *
 * The upstream file defines:
 *
 * - the implementation of the `Context` trait for `Result` and `Option`;
 * - the `ContextError<C, E>` formatting and `StdError` integration;
 * - a `private::Sealed` gate so the public `Context` trait stays sealed.
 */

/**
 * Equivalent of the upstream `ext` module.
 *
 * The Rust implementation uses trait specialization-like dispatch to attach context while
 * preserving a backtrace and the source chain. Kotlin has no blanket trait impls; this port keeps
 * the same separation by routing through an internal adapter interface.
 */
internal object ext {
    internal interface StdError {
        fun extContext(context: Any): Error
    }
}

internal fun Throwable.extContext(context: Any): Error {
    if (this is Error) {
        return this.context(context)
    }

    val backtrace = backtraceIfAbsent(this)
    return Error.constructFromContext(context, throwableAsStdError(this), backtrace)
}

/**
 * Implementation of `Context` for `Result<T, E>` in the upstream.
 *
 * Kotlin's `Result<T>` does not carry an explicit error type parameter; failures are represented
 * by a [Throwable]. This translation models the same behavior by attaching context onto the
 * existing thrown value.
 */
public fun <T> Result<T>.context(context: Any): Result<T> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return this
    }

    val error = exceptionOrNull() ?: return this
    return Result.failure(error.extContext(context))
}

public fun <T> Result<T>.withContext(context: () -> Any): Result<T> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return this
    }

    val error = exceptionOrNull() ?: return this
    return Result.failure(error.extContext(context()))
}

/**
 * Equivalent of `Option<T>::context` / `Option<T>::with_context` in the upstream.
 *
 * ```kotlin
 * typealias T = Unit
 *
 * fun maybeGet(): T? {
 *     return null
 * }
 *
 * fun demo(): Result<Unit> {
 *     val t = maybeGet().context("there is no T").getOrThrow()
 *     return Ok(Unit)
 * }
 * ```
 */
public fun <T> T?.context(context: Any): Result<T> {
    return when (this) {
        null -> Result.failure(Error.constructFromDisplay(context, backtrace()))
        else -> Ok(this)
    }
}

public fun <T> T?.withContext(context: () -> Any): Result<T> {
    return when (this) {
        null -> Result.failure(Error.constructFromDisplay(context(), backtrace()))
        else -> Ok(this)
    }
}

/**
 * Kotlin analog of the upstream formatting behavior for `ContextError<C, E>`.
 *
 * In Rust, Debug formatting renders:
 *
 * ```text
 * Error { context: "...", source: ... }
 * ```
 *
 * while Display formatting prints only the context message.
 */
internal fun <C, E : StdError> contextErrorDebug(contextError: ContextError<C, E>): String {
    val out = StringBuilder()
    out.append("Error")
    out.append(" { ")
    out.append("context: ")
    out.append(Quoted(contextError.context).toString())
    out.append(", ")
    out.append("source: ")
    out.append(contextError.error.toString())
    out.append(" }")
    return out.toString()
}

/**
 * Kotlin translation of the upstream `Quoted` adapter used by `ContextError` debug output.
 */
internal class Quoted<C>(
    private val value: C,
) {
    override fun toString(): String {
        val out = StringBuilder()
        out.append('"')
        QuotedWriter(out).writeStr(value.toString())
        out.append('"')
        return out.toString()
    }

    private class QuotedWriter(
        private val out: Appendable,
    ) {
        fun writeStr(s: String) {
            for (ch in s) {
                when (ch) {
                    '\n' -> out.append("\\n")
                    '\r' -> out.append("\\r")
                    '\t' -> out.append("\\t")
                    '\"' -> out.append("\\\"")
                    '\\' -> out.append("\\\\")
                    else -> out.append(ch)
                }
            }
        }
    }
}

/**
 * Kotlin translation of the upstream `private` module used to seal the public `Context` trait.
 */
internal object `private` {
    internal interface Sealed

    internal class SealedResult<T>(
        private val value: Result<T>,
    ) : Sealed

    internal class SealedOption<T>(
        private val value: T?,
    ) : Sealed
}
