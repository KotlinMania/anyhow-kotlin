@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source lib.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

/*
 * This library provides [Error], a dynamic error type for easy idiomatic error handling
 * in Kotlin applications.
 *
 * # Details
 *
 * - Use [Result] as the return type of any fallible function.
 *
 *   Within the function, propagate failures by throwing.
 *
 *   ```kotlin
 *   fun getClusterInfo(): Result<ClusterMap> = runCatching {
 *       val config = readToString("cluster.json")
 *       SerdeJson.fromStr<ClusterMap>(config)
 *   }
 *   ```
 *
 * - Attach context to help the person troubleshooting the error understand where things went
 *   wrong. A low-level error like "No such file or directory" can be annoying to debug without
 *   more context about what higher level step the application was in the middle of.
 *
 *   ```kotlin
 *   fun main(): Result<Unit> = runCatching {
 *       it.detach().context("Failed to detach the important thing").getOrThrow()
 *       val content = readBytes(path)
 *           .withContext { "Failed to read instrs from $path" }
 *           .getOrThrow()
 *   }
 *   ```
 *
 *   ```text
 *   Error: Failed to read instrs from ./path/to/instrs.json
 *
 *   Caused by:
 *       No such file or directory
 *   ```
 *
 * - Downcasting is supported and can be done by value, by shared reference, or by mutable reference
 *   as needed.
 *
 *   ```kotlin
 *   sealed class DataStoreError : Throwable() {
 *       class Censored : DataStoreError()
 *   }
 *
 *   fun demo(error: Error): Result<Unit> {
 *       return when (error.downcastRef<DataStoreError>()) {
 *           is DataStoreError.Censored -> Result.success(Unit)
 *           else -> Result.failure(error)
 *       }
 *   }
 *   ```
 *
 * - A backtrace is captured and printed with the error if the underlying error type does not
 *   already provide its own.
 *
 * - This library works with error types modeled as [Throwable] or types participating in the
 *   [StdError] surface.
 *
 * - One-off error messages can be constructed using helpers like [anyhow] and [bail].
 *
 * # Targets
 *
 * Kotlin/Native, Kotlin/JVM, and Kotlin/JS runtimes.
 */

/**
 * A minimal "standard error" abstraction used throughout the anyhow library.
 * Provides a [source] chain for causal errors.
 */
@HiddenFromObjC
public interface StdError {
    public fun source(): StdError? = null
}

/**
 * The [Error] type, a wrapper around a dynamic error type.
 *
 * [Error] works a lot like a boxed [StdError], but with these differences:
 *
 * - [Error] requires that the error is thread-safe.
 * - [Error] guarantees that a backtrace is available, even if the underlying
 *   error type does not provide one.
 * - [Error] is represented as a narrow pointer — exactly one word in size
 *   instead of two.
 *
 * ## Display representations
 *
 * When you print an error object using `toString()`, only the outermost underlying error or
 * context is printed, not any of the lower level causes.
 *
 * ```text
 * Failed to read instrs from ./path/to/instrs.json
 * ```
 *
 * To print causes as well using anyhow's default formatting of causes, use the alternate
 * representation (modeled by this library’s formatting helpers).
 *
 * ```text
 * Failed to read instrs from ./path/to/instrs.json: No such file or directory
 * ```
 *
 * The debug format includes your backtrace if one was captured.
 *
 * ```text
 * Error: Failed to read instrs from ./path/to/instrs.json
 *
 * Caused by:
 *     No such file or directory
 * ```
 *
 * If none of the built-in representations are appropriate and you would prefer to render the
 * error and its cause chain yourself, it can be done something like this:
 *
 * ```kotlin
 * fun main() {
 *     val result = tryMain()
 *     val err = result.exceptionOrNull()
 *     if (err is Error) {
 *         println("ERROR: ${err}")
 *         for (cause in err.chain().drop(1)) {
 *             println("because: $cause")
 *         }
 *     }
 * }
 *
 * fun tryMain(): Result<Unit> = runCatching {
 *     Unit
 * }
 * ```
 */
@HiddenFromObjC
public class Error internal constructor(
    internal val inner: Own<ErrorImpl>,
) : Throwable(),
    StdError {
    override fun source(): StdError? = errorSource(this)

    override fun toString(): String = display(inner.byRef(), alternate = false)

    public companion object
}

/**
 * Type alias for [kotlin.Result] with failure pinned to [Error].
 */
public typealias Result<T> = kotlin.Result<T>

/**
 * Provides the `context` method for `Result`.
 *
 * Upstream, this trait is sealed and cannot be implemented for types outside of the library.
 *
 * # Example
 *
 * ```kotlin
 * fun doIt(it: ImportantThing): Result<ByteArray> = runCatching {
 *     it.detach().context("Failed to detach the important thing").getOrThrow()
 *     readBytes(it.path)
 *         .withContext { "Failed to read instrs from ${it.path}" }
 *         .getOrThrow()
 * }
 * ```
 *
 * When printed, the outermost context would be printed first and the lower level underlying
 * causes would be enumerated below.
 *
 * ```text
 * Error: Failed to read instrs from ./path/to/instrs.json
 *
 * Caused by:
 *     No such file or directory
 * ```
 *
 * Refer to the "Display representations" documentation on [Error] for other forms in which this
 * context chain can be rendered.
 *
 * # Effect on downcasting
 *
 * After attaching context of type `C` onto an error of type `E`, the resulting [Error] may be
 * downcast to `C` **or** to `E`.
 *
 * That is, in codebases that rely on downcasting, Anyhow's context supports both of the following
 * use cases:
 *
 * - **Attaching context whose type is insignificant onto errors whose type is used in downcasts.**
 *
 *   In other error libraries whose context is not designed this way, it can be risky to introduce
 *   context to existing code because new context might break existing working downcasts. In Anyhow,
 *   any downcast that worked before adding context will continue to work after you add a context, so
 *   you should freely add human-readable context to errors wherever it would be helpful.
 *
 *   ```kotlin
 *   fun doIt(): Result<Unit> = runCatching {
 *       helper().context("...").getOrThrow()
 *   }
 *
 *   val err = doIt().exceptionOrNull()
 *   if (err is Error) {
 *       val suspicious = err.downcastRef<SuspiciousError>()
 *       // If helper() returned SuspiciousError, this downcast will correctly succeed
 *       // even with the context in between.
 *   }
 *   ```
 *
 * - **Attaching context whose type is used in downcasts onto errors whose type is insignificant.**
 *
 *   Some codebases prefer to use machine-readable context to categorize lower level errors in a way
 *   that will be actionable to higher levels of the application.
 *
 *   ```kotlin
 *   fun doIt(): Result<Unit> = runCatching {
 *       helper().context(HelperFailed).getOrThrow()
 *   }
 *
 *   val err = doIt().exceptionOrNull()
 *   if (err is Error) {
 *       val helperFailed = err.downcastRef<HelperFailed>()
 *       // If helper failed, this downcast will succeed because HelperFailed is the
 *       // context that has been attached to that error.
 *   }
 *   ```
 */
@HiddenFromObjC
public interface Context<T, E> {
    /**
     * Wrap the error value with additional context.
     */
    public fun context(context: Any): Result<T>

    /**
     * Wrap the error value with additional context that is evaluated lazily only once an error does
     * occur.
     */
    public fun withContext(f: () -> Any): Result<T>
}

/**
 * Equivalent to `Result.success(value)` for an anyhow [Result], with the failure
 * type pinned to [Error] without explicit type arguments.
 */
@HiddenFromObjC
public fun <T> Ok(value: T): Result<T> = Result.success(value)

// Not public API. Used by the [anyhow], [bail], and [ensure] helpers.
@HiddenFromObjC
public object __private {
    public fun formatErr(message: String): Error = anyhow(message)

    public fun mustUse(error: Error): Error = error

    public fun not(cond: Boolean): Boolean = !cond
}
