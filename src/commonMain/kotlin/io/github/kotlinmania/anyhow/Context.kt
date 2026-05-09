// port-lint: source context.rs
package io.github.kotlinmania.anyhow

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

public fun <T> T?.toResult(context: Any): Result<T> {
    return when (this) {
        null -> Result.failure(Error.constructFromDisplay(context, backtrace()))
        else -> Ok(this)
    }
}

public fun <T> T?.toResultWith(context: () -> Any): Result<T> {
    return when (this) {
        null -> Result.failure(Error.constructFromDisplay(context(), backtrace()))
        else -> Ok(this)
    }
}

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
            out.append(s.escapeDebug())
        }
    }
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

internal object `private` {
    internal interface Sealed

    internal class SealedResult<T>(
        private val value: Result<T>,
    ) : Sealed

    internal class SealedOption<T>(
        private val value: T?,
    ) : Sealed
}
