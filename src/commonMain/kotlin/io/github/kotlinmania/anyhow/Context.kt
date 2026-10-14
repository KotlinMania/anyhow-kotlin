@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source context.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

internal object ext {
    internal interface StdError {
        fun extContext(context: Any): Error
    }
}

internal fun StdError.extContext(context: Any): Error {
    val backtrace = backtraceIfAbsent(this)
    return Error.constructFromContext(context, this, backtrace)
}

internal fun Error.extContext(context: Any): Error = this.context(context)

internal fun Throwable.extContext(context: Any): Error {
    if (this is Error) {
        return this.context(context)
    }

    val backtrace = backtraceIfAbsent(this)
    return Error.constructFromContext(context, throwableAsStdError(this), backtrace)
}

@HiddenFromObjC
public fun <T> Result<T>.context(context: Any): Result<T> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return this
    }

    val error = exceptionOrNull() ?: return this
    return Result.failure(error.extContext(context))
}

@HiddenFromObjC
public fun <T> Result<T>.withContext(context: () -> Any): Result<T> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return this
    }

    val error = exceptionOrNull() ?: return this
    return Result.failure(error.extContext(context()))
}

/**
 * Wrap a nullable value in a [Result], attaching context when `null`.
 *
 * ```kotlin
 * fun maybeGet(): Unit? = null
 *
 * fun demo(): Result<Unit> = maybeGet().toResult("there is no Unit")
 * ```
 */
@HiddenFromObjC
public fun <T> T?.toResult(context: Any): Result<T> =
    when (this) {
        null -> Result.failure(Error.constructFromDisplay(context, backtrace()))
        else -> Ok(this)
    }

@HiddenFromObjC
public fun <T> T?.toResultWith(context: () -> Any): Result<T> =
    when (this) {
        null -> Result.failure(Error.constructFromDisplay(context(), backtrace()))
        else -> Ok(this)
    }

internal fun <C, E : StdError> ContextError<C, E>.fmt(
    f: Appendable,
    debug: Boolean,
): Appendable {
    if (!debug) {
        return fmt(f)
    }
    f.append("Error")
    f.append(" { ")
    f.append("context: ")
    Quoted(context).fmt(f)
    f.append(", ")
    f.append("source: ")
    f.append(error.toString())
    f.append(" }")
    return f
}

internal fun <C, E : StdError> ContextError<C, E>.fmt(f: Appendable): Appendable {
    f.append(context.toString())
    return f
}

internal fun <C, E : StdError> source(contextError: ContextError<C, E>): StdError? = contextError.error

internal fun <C, E : StdError> ContextError<C, E>.provide(request: Nightly.Request) {
    Nightly.provide(error, request)
}

internal fun <C> ContextError<C, Error>.source(chain: Boolean): StdError? =
    if (chain) errorImplError(error.inner.byRef()) else error

internal fun <C> ContextError<C, Error>.provide(
    request: Nightly.Request,
    chain: Boolean,
) {
    if (chain) {
        error.provide(request)
    }
}

internal fun <C, E : StdError> contextErrorDebug(contextError: ContextError<C, E>): String =
    contextError.fmt(StringBuilder(), debug = true).toString()

internal class Quoted<C>(
    internal val value: C,
) {
    override fun toString(): String = fmt(StringBuilder()).toString()
}

internal fun <C> Quoted<C>.fmt(formatter: Appendable): Appendable {
    formatter.append('"')
    QuotedWriter(formatter).writeStr(value.toString())
    formatter.append('"')
    return formatter
}

private class QuotedWriter(
    val out: Appendable,
)

private fun QuotedWriter.writeStr(s: String) {
    out.append(s.escapeDebug())
}

private fun String.escapeDebug(): String {
    val out = StringBuilder()
    for (ch in this) {
        out.append(ch.escapeDebug())
    }
    return out.toString()
}

private fun Char.escapeDebug(): String =
    when (this) {
        '\t' -> "\\t"
        '\r' -> "\\r"
        '\n' -> "\\n"
        '\\' -> "\\\\"
        '\'' -> "\\'"
        '"' -> "\\\""
        in ' '..'~' -> this.toString()
        else -> "\\u{" + code.toString(16) + "}"
    }

internal interface Sealed

internal class SealedResult<T>(
    private val value: Result<T>,
) : Context<T, Error>,
    Sealed {
    override fun context(context: Any): Result<T> = value.context(context)

    override fun withContext(f: () -> Any): Result<T> = value.withContext(f)
}

internal class SealedOption<T>(
    private val value: T?,
) : Context<T, Nothing>,
    Sealed {
    override fun context(context: Any): Result<T> =
        when (val v = value) {
            null -> Result.failure(Error.constructFromDisplay(context, backtrace()))
            else -> Ok(v)
        }

    override fun withContext(f: () -> Any): Result<T> =
        when (val v = value) {
            null -> Result.failure(Error.constructFromDisplay(f(), backtrace()))
            else -> Ok(v)
        }
}
