// port-lint: source src/lib.rs
package io.github.kotlinmania.anyhow

/**
 * This library provides [Error], a trait-object-like error type for easy idiomatic error handling
 * in Kotlin applications.
 *
 * The upstream project is the Rust crate `anyhow`. Rust relies on `std::error::Error`, the `?`
 * operator, macro calls like `anyhow!(...)` / `bail!(...)`, and conditional compilation. Kotlin has no direct
 * analog for Rust macros or conditional compilation, so this port represents the same API surface
 * using normal Kotlin declarations.
 *
 * # Details
 *
 * - Use `Result<T>` (this port’s [Result]) as the return type of any fallible function.
 *
 *   Within the function, use `runCatching { ... }` (or whatever error propagation helper your
 *   Kotlin codebase uses) to propagate failures as exceptions.
 *
 *   ```kotlin
 *   interface Deserialize
 *
 *   object SerdeJson {
 *       fun <T : Deserialize> fromString(json: String): T {
 *           error("unimplemented")
 *       }
 *   }
 *
 *   class ClusterMap : Deserialize
 *
 *   fun getClusterInfo(): Result<ClusterMap> = runCatching {
 *       val config = readText("cluster.json")
 *       val map: ClusterMap = SerdeJson.fromString(config)
 *       map
 *   }
 *   ```
 *
 * - Attach context to help the person troubleshooting the error understand where things went
 *   wrong. A low-level error like "No such file or directory" can be annoying to debug without
 *   more context about what higher level step the application was in the middle of.
 *
 *   ```kotlin
 *   class It {
 *       fun detach(): Result<Unit> = runCatching {
 *           error("unimplemented")
 *       }
 *   }
 *
 *   fun main(): Result<Unit> = runCatching {
 *       val it = It()
 *       val path = "./path/to/instrs.json"
 *
 *       it.detach().getOrThrow()
 *       val content = readBytes(path)
 *       content
 *   }.map { Unit }
 *   ```
 *
 *   ```text
 *   Error: Failed to read instrs from ./path/to/instrs.json
 *
 *   Caused by:
 *       No such file or directory
 *   ```
 *
 * - Downcasting is supported and can be by value, by shared reference, or by mutable reference as
 *   needed. In Kotlin, this corresponds to `is` checks and safe casts (`as?`) over the dynamic
 *   underlying error type.
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
 *   already provide its own. In Kotlin, stack traces are available through [Throwable.stackTrace]
 *   and are surfaced by this port through its backtrace helpers.
 *
 * - Anyhow works with any error type that implements Rust's `std::error::Error`, including ones
 *   defined in your crate. In Kotlin, this port works with error types modeled as [Throwable] or
 *   types participating in the [StdError] surface.
 *
 * - One-off error messages can be constructed using the upstream macro `anyhow!(...)`, which
 *   supports string interpolation and produces an error. Kotlin has no macro system; this port provides
 *   equivalent helpers as functions in `Macros.kt`.
 *
 * # No-std support
 *
 * In the upstream, `no_std` builds are supported. Kotlin Multiplatform has a different runtime
 * model and does not have an equivalent `no_std` mode; this port targets Kotlin/Native, Kotlin/JVM,
 * and Kotlin/JS runtimes instead.
 */

/**
 * A minimal "standard error" abstraction used throughout the anyhow port.
 *
 * The Rust upstream uses `std::error::Error` (or `core::error::Error`) and provides a `source()`
 * chain for causal errors. Kotlin's closest built-in abstraction is [Throwable]; this interface
 * exists so the port can keep the upstream's naming and call patterns (`source`, `chain`, etc.).
 */
public interface StdError {
    public fun source(): StdError? = null
}

/**
 * The `Error` type, a wrapper around a dynamic error type.
 *
 * Upstream, `Error` behaves similarly to a boxed `Throwable` with these differences:
 *
 * - `Error` requires that the error is thread-safe and long-lived in Rust terms;
 * - `Error` guarantees that a backtrace is available, even if the underlying error type does not
 *   provide one;
 * - `Error` is represented as a narrow pointer (one word) rather than a fat pointer.
 *
 * Kotlin does not have the same trait-object pointer story; this port retains the structure and
 * API shape while representing errors in terms of Kotlin objects.
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
 * representation (modeled by this port’s formatting helpers).
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
public class Error internal constructor(
    internal val inner: Own<ErrorImpl>,
) : Throwable(), StdError {
    override fun source(): StdError? = errorSource(this)

    override fun toString(): String = ErrorImplDisplay(inner.byRef(), alternate = false)

    public companion object
}

/**
 * Iterator of a chain of source errors.
 *
 * This type is the iterator returned by `Error.chain()` in the upstream API.
 *
 * # Example
 *
 * ```kotlin
 * fun underlyingIoErrorKind(error: Error): Throwable? {
 *     for (cause in error.chain()) {
 *         val ioError = cause as? Throwable
 *         if (ioError != null) return ioError
 *     }
 *     return null
 * }
 * ```
 */
public class Chain internal constructor(
    internal var state: ChainState,
) : Iterator<StdError> {
    override fun hasNext(): Boolean = chainHasNext(this)

    override fun next(): StdError = chainNext(this)

    public fun nextBack(): StdError? = chainNextBack(this)

    public fun len(): Int = chainLen(this)

    public companion object {
        public fun default(): Chain = chainDefault()
    }
}

/**
 * `Result<T, Error>` in the upstream.
 *
 * Kotlin's standard library [kotlin.Result] carries failure as a [Throwable]. This port uses that
 * representation and models anyhow's error as a [Throwable]-derived [Error].
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
 * class ImportantThing(val path: String) {
 *     fun detach(): Result<Unit> = runCatching { Unit }
 * }
 *
 * fun doIt(it: ImportantThing): Result<ByteArray> = runCatching {
 *     it.detach().getOrThrow()
 *     readBytes(it.path)
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
 *   class SuspiciousError : Throwable()
 *
 *   fun helper(): Result<Unit> = Result.failure(SuspiciousError())
 *
 *   fun doIt(): Result<Unit> = runCatching {
 *       helper().getOrThrow()
 *   }
 *
 *   fun main() {
 *       val err = doIt().exceptionOrNull()
 *       if (err is Error) {
 *           val suspicious = err.downcastRef<SuspiciousError>()
 *           if (suspicious != null) {
 *               // If helper() returned SuspiciousError, this downcast will correctly succeed even
 *               // with the context in between.
 *               return
 *           }
 *       }
 *       error("expected downcast to succeed")
 *   }
 *   ```
 *
 * - **Attaching context whose type is used in downcasts onto errors whose type is insignificant.**
 *
 *   Some codebases prefer to use machine-readable context to categorize lower level errors in a way
 *   that will be actionable to higher levels of the application.
 *
 *   ```kotlin
 *   class HelperFailed : Throwable()
 *
 *   fun helper(): Result<Unit> = Result.failure(RuntimeException("no such file or directory"))
 *
 *   fun doIt(): Result<Unit> = runCatching {
 *       helper().getOrThrow()
 *   }
 *
 *   fun main() {
 *       val err = doIt().exceptionOrNull()
 *       if (err is Error) {
 *           val helperFailed = err.downcastRef<HelperFailed>()
 *           if (helperFailed != null) {
 *               // If helper failed, this downcast will succeed because HelperFailed is the context
 *               // that has been attached to that error.
 *               return
 *           }
 *       }
 *       error("expected downcast to succeed")
 *   }
 *   ```
 */
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
 * Equivalent to `Ok::<_, anyhow::Error>(value)` in the upstream.
 *
 * This simplifies creation of an [Result] in places where type inference cannot deduce the error
 * type of a result, without needing explicit type arguments.
 */
public object Ok {
    public operator fun <T> invoke(value: T): Result<T> = Result.success(value)
}

// Not public API. Referenced by macro-generated code in the upstream.
public object __private {
    public fun formatErr(message: String): Error = anyhow(message)

    public fun mustUse(error: Error): Error = error

    public fun not(cond: Boolean): Boolean = !cond
}
