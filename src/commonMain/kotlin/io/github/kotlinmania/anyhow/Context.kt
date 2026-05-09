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

// Upstream Rust defines `impl<T> Context<T, Infallible> for Option<T>` alongside the
// Result impl above. In Kotlin, the receiver and `Any`/`() -> Any` parameter both
// erase on JVM, so a literal port `T?.context(Any)` clashes with the Result version
// (both erase to `(Object, Object)`). Renaming the Option-receiver pair to
// `toResult` / `toResultWith` keeps the same semantic ("None -> Err, Some -> Ok")
// while giving the JVM compiler a distinct method name.
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

internal object `private` {
    internal interface Sealed

    internal class SealedResult<T>(
        private val value: Result<T>,
    ) : Sealed

    internal class SealedOption<T>(
        private val value: T?,
    ) : Sealed
}
