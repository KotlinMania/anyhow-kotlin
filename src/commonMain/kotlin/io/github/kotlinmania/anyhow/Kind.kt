// port-lint: source src/kind.rs
package io.github.kotlinmania.anyhow

/**
 * Tagged dispatch mechanism for resolving the behavior of `anyhow!(expr)` in the upstream.
 *
 * In Rust, the `anyhow!` macro uses method-resolution tricks to choose between:
 *
 * - constructing an adhoc message error for values that are only displayable/debuggable, or
 * - using an existing standard error implementation (preserving `source()` and backtrace),
 * - or constructing from a boxed dynamic error.
 *
 * Kotlin has no macro system and no equivalent of Rust's autoref-based specialization. This file
 * preserves the upstream structure so that the Kotlin `anyhow(...)` helpers can select the same
 * construction paths explicitly.
 */

public object Adhoc

/**
 * Kotlin analog of the upstream `AdhocKind` trait.
 *
 * In Rust, this is implemented for `&T` where `T` is displayable and debuggable. Kotlin does not
 * distinguish display vs debug; callers can opt into this path explicitly.
 */
public interface AdhocKind {
    public fun anyhowKind(): Adhoc = Adhoc
}

public fun Adhoc.new(message: Any): Error {
    return Error.constructFromAdhoc(message, backtrace())
}

public object Trait

/**
 * Kotlin analog of the upstream `TraitKind` trait.
 *
 * In Rust, this is implemented when `E: Into<Error>` so that existing error types can be converted
 * directly into anyhow's `Error`.
 */
public interface TraitKind {
    public fun anyhowKind(): Trait = Trait
}

public fun <E : ErrorConvertible> Trait.new(error: E): Error = error.intoError()

/**
 * Kotlin analog of the upstream `BoxedKind` trait.
 */
public interface BoxedKind {
    public fun anyhowKind(): Boxed = Boxed
}

public object Boxed

public fun Boxed.new(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error)
    return Error.constructFromBoxed(error, backtrace)
}

/**
 * Helper interface for values that can be converted into [Error], corresponding to Rust's `Into<Error>`.
 */
public interface ErrorConvertible {
    public fun intoError(): Error
}
