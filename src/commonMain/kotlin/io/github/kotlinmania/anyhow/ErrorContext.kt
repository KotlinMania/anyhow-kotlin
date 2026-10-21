package io.github.kotlinmania.anyhow

/*
 * This library provides [Error], a dynamic error type for easy idiomatic error handling
 * in Kotlin applications.
 *
 * # Details
 *
 * - Use [Result] as the return type of any fallible function.
 *
 * - Attach context to help the person troubleshooting the error understand where things went
 *   wrong. A low-level error like "No such file or directory" can be annoying to debug without
 *   more context about what higher level step the application was in the middle of.
 *
 * - Downcasting is supported and can be done by value, by shared reference, or by mutable reference
 *   as needed.
 *
 * - A backtrace is captured and printed with the error if the underlying error type does not
 *   already provide its own.
 *
 * - This library works with error types modeled as [Throwable] or types participating in the
 *   [StdError] surface.
 *
 * - One-off error messages can be constructed using helpers like [anyhow] and [bail].
 */

/**
 * A minimal "standard error" abstraction used throughout the anyhow library.
 * Provides a [source] chain for causal errors.
 */
public interface StdError {
    public fun source(): StdError? = null

    public fun debugString(alternate: Boolean = false): String = toString()
}

/**
 * A custom diagnostic payload used to verify that [Result] can hold arbitrary error types
 * and preserve object identity across language boundaries.
 */
public class CustomDiagnostic(
    public val code: Int,
    public val message: String,
) {
    override fun toString(): String = "CustomDiagnostic(code=$code, message=$message)"
}

/**
 * The [Error] type, a wrapper around a dynamic error type.
 *
 * Upstream Rust `anyhow::Error` is a trait-object based error wrapper that dereferences
 * to `dyn std::error::Error`. In Kotlin, it implements [StdError] without inheriting from [Throwable].
 *
 * - [Error] requires that the error is thread-safe.
 * - [Error] guarantees that a backtrace is available, even if the underlying
 *   error type does not provide one.
 * - [Error] is represented as a narrow pointer — exactly one word in size
 *   instead of two.
 */
public class Error internal constructor(
    internal val inner: Own<ErrorImpl>,
) : StdError {
    override fun source(): StdError? = errorSource(this)

    override fun toString(): String = display(inner.byRef(), alternate = false)

    override fun debugString(alternate: Boolean): String = debug(inner.byRef(), alternate = alternate)

    public companion object
}

/**
 * Exception wrapper used when [Result.getOrThrow] is invoked on an [Error] or non-[Throwable] failure.
 */
internal class AnyhowException(
    public val error: Error,
) : RuntimeException(error.toString()) {
    override val cause: Throwable?
        get() = (error.source() as? ThrowableStdError)?.value
}

/**
 * `Result<T, E>`
 *
 * A disjoint union of [Ok] containing [value] of type [T] or [Err] containing [error] of type [E].
 *
 * Upstream Rust defines `pub type Result<T, E = Error> = core::result::Result<T, E>`.
 * Note: Kotlin does not support default type parameters on classes or arity-overloaded type aliases
 * in the same package scope, so both [T] and [E] type parameters are explicit.
 * For the common case where [E] is [Error], use `Result<T, Error>`.
 */
public sealed class Result<out T, out E> {
    public class Ok<out T>(
        public val value: T,
    ) : Result<T, Nothing>() {
        override val isSuccess: Boolean get() = true

        override fun getOrNull(): T = value

        override fun toString(): String = "Result.Ok($value)"

        override fun equals(other: Any?): Boolean = other is Ok<*> && value == other.value

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }

    public class Err<out E>(
        public val error: E,
    ) : Result<Nothing, E>() {
        override val isFailure: Boolean get() = true

        override fun exceptionOrNull(): E = error

        override fun toString(): String = "Result.Err($error)"

        override fun equals(other: Any?): Boolean = other is Err<*> && error == other.error

        override fun hashCode(): Int = error?.hashCode() ?: 0
    }

    public open val isSuccess: Boolean get() = false
    public open val isFailure: Boolean get() = false

    public open fun getOrNull(): T? = null

    public open fun exceptionOrNull(): E? = null

    public companion object {
        public fun <T> ok(value: T): Result<T, Nothing> = Ok(value)

        public fun <E> err(error: E): Result<Nothing, E> = Err(error)
    }
}

public fun <T, E> Result<T, E>.getOrThrow(): T =
    when (this) {
        is Result.Ok -> {
            value
        }

        is Result.Err -> {
            when (val err = error) {
                is Throwable -> throw err
                is Error -> throw AnyhowException(err)
                else -> error("Result failed with error: $err")
            }
        }
    }

/**
 * Functional interface for supplying context dynamically without raw Kotlin function types.
 */
public fun interface ContextSupplier {
    public operator fun invoke(): Any
}

/**
 * Provides the `context` method for [Result].
 */
public sealed interface Context<out T, out E> {
    /**
     * Wrap the error value with additional context.
     */
    public fun context(context: Any): Result<T, Error>

    /**
     * Wrap the error value with additional context that is evaluated lazily only once an error does
     * occur.
     */
    public fun withContext(context: ContextSupplier): Result<T, Error>
}

public inline fun <T, E> Context<T, E>.withContext(crossinline context: () -> Any): Result<T, Error> =
    withContext(ContextSupplier { context() })

/**
 * Shorthand helper matching Rust `pub fn Ok<T>(value: T) -> Result<T>`,
 * fixing the error parameter to [Error].
 */
public fun <T> Ok(value: T): Result<T, Error> = Result.Ok(value)

internal interface Bool {
    fun not(): Boolean
}

internal fun formatErr(message: String): Error = anyhow(message)

internal fun mustUse(error: Error): Error = error

internal fun not(cond: Bool): Boolean = cond.not()

internal fun not(cond: Boolean): Boolean = !cond

// Not public API. Used by the [anyhow], [bail], and [ensure] helpers.
public object __private {
    public fun formatErr(message: String): Error = Error.msg(message)

    public fun mustUse(error: Error): Error = error

    public fun not(cond: Boolean): Boolean = !cond
}
