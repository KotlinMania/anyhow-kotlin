// port-lint: source context.rs

package io.github.kotlinmania.anyhow

internal object ext {
    internal interface StdError {
        fun extContext(context: Any): Error
    }
}

internal fun StdError.extContext(context: Any): Error {
    if (this is Error) {
        return this.context(context)
    }
    val backtrace = backtraceIfAbsent(this)
    return Error.constructFromContext(context, this, backtrace)
}

internal fun Error.extContext(context: Any): Error = this.context(context)

internal fun Throwable.extContext(context: Any): Error {
    if (this is AnyhowException) {
        return this.error.context(context)
    }

    val backtrace = backtraceIfAbsent(this)
    return Error.constructFromContext(context, throwableAsStdError(this), backtrace)
}

public fun <T, E : StdError> Result<T, E>.context(context: Any): Result<T, Error> =
    when (this) {
        is Result.Ok -> Result.Ok(value)
        is Result.Err -> Result.Err(error.extContext(context))
    }

public fun <T, E : StdError> Result<T, E>.withContext(context: ContextSupplier): Result<T, Error> =
    when (this) {
        is Result.Ok -> Result.Ok(value)
        is Result.Err -> Result.Err(error.extContext(context()))
    }

public inline fun <T, E : StdError> Result<T, E>.withContext(crossinline context: () -> Any): Result<T, Error> =
    withContext(ContextSupplier { context() })

@kotlin.jvm.JvmName("contextThrowable")
public fun <T, E : Throwable> Result<T, E>.context(context: Any): Result<T, Error> =
    when (this) {
        is Result.Ok -> Result.Ok(value)
        is Result.Err -> Result.Err(error.extContext(context))
    }

@kotlin.jvm.JvmName("withContextThrowable")
public fun <T, E : Throwable> Result<T, E>.withContext(context: ContextSupplier): Result<T, Error> =
    when (this) {
        is Result.Ok -> Result.Ok(value)
        is Result.Err -> Result.Err(error.extContext(context()))
    }

@kotlin.jvm.JvmName("withContextThrowableLambda")
public inline fun <T, E : Throwable> Result<T, E>.withContext(crossinline context: () -> Any): Result<T, Error> =
    withContext(ContextSupplier { context() })

public fun <T> kotlin.Result<T>.context(context: Any): Result<T, Error> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return Result.ok(getOrThrow())
    }

    val error = exceptionOrNull() ?: return Result.err(anyhow("unknown failure"))
    return Result.err(error.extContext(context))
}

public fun <T> kotlin.Result<T>.withContext(context: ContextSupplier): Result<T, Error> {
    val ok = getOrNull()
    if (ok != null || isSuccess) {
        return Result.ok(getOrThrow())
    }

    val error = exceptionOrNull() ?: return Result.err(anyhow("unknown failure"))
    return Result.err(error.extContext(context()))
}

public inline fun <T> kotlin.Result<T>.withContext(crossinline context: () -> Any): Result<T, Error> =
    withContext(ContextSupplier { context() })

/**
 * Wrap a nullable value in a [Result], attaching context when `null`.
 *
 * ```kotlin
 * fun maybeGet(): Unit? = null
 *
 * fun demo(): Result<Unit, Error> = maybeGet().toResult("there is no Unit")
 * ```
 */
public fun <T> T?.toResult(context: Any): Result<T, Error> =
    when (this) {
        null -> Result.err(Error.constructFromDisplay(context, backtrace()))
        else -> Ok(this)
    }

public fun <T> T?.toResultWith(context: ContextSupplier): Result<T, Error> =
    when (this) {
        null -> Result.err(Error.constructFromDisplay(context(), backtrace()))
        else -> Ok(this)
    }

public inline fun <T> T?.toResultWith(crossinline context: () -> Any): Result<T, Error> =
    toResultWith(ContextSupplier { context() })

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

internal fun String.escapeDebug(): String {
    val out = StringBuilder()
    for (ch in this) {
        out.append(ch.escapeDebug())
    }
    return out.toString()
}

internal fun Char.escapeDebug(): String =
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

internal class SealedResult<T, E : StdError>(
    private val value: Result<T, E>,
) : Context<T, E>,
    Sealed {
    override fun context(context: Any): Result<T, Error> = value.context(context)

    override fun withContext(context: ContextSupplier): Result<T, Error> = value.withContext(context)
}

internal class SealedResultThrowable<T, E : Throwable>(
    private val value: Result<T, E>,
) : Context<T, E>,
    Sealed {
    override fun context(context: Any): Result<T, Error> = value.context(context)

    override fun withContext(context: ContextSupplier): Result<T, Error> = value.withContext(context)
}

internal class SealedOption<T>(
    private val value: T?,
) : Context<T, Nothing>,
    Sealed {
    override fun context(context: Any): Result<T, Error> =
        when (val v = value) {
            null -> Result.err(Error.constructFromDisplay(context, backtrace()))
            else -> Ok(v)
        }

    override fun withContext(context: ContextSupplier): Result<T, Error> =
        when (val v = value) {
            null -> Result.err(Error.constructFromDisplay(context(), backtrace()))
            else -> Ok(v)
        }
}
