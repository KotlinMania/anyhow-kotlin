// port-lint: source kind.rs
package io.github.kotlinmania.anyhow

/**
 * Tagged dispatch mechanism for resolving the behavior of [anyhow] given a single expression.
 *
 * Three [Error] construction paths are available, chosen by which marker interface
 * the input value satisfies:
 *
 * - [AdhocKind] — construct an adhoc message error for values that are only displayable.
 * - [TraitKind] — use an existing [StdError] implementation, preserving its [source] and backtrace.
 * - [BoxedKind] — construct from a boxed dynamic [StdError].
 */

public object Adhoc

public interface AdhocKind {
    public fun anyhowKind(): Adhoc = Adhoc
}

public fun Adhoc.new(message: Any): Error {
    return Error.constructFromAdhoc(message, backtrace())
}

public object Trait

public interface TraitKind {
    public fun anyhowKind(): Trait = Trait
}

public fun <E : ErrorConvertible> Trait.new(error: E): Error = error.intoError()

public interface BoxedKind {
    public fun anyhowKind(): Boxed = Boxed
}

public object Boxed

public fun Boxed.new(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error)
    return Error.constructFromBoxed(error, backtrace)
}

/**
 * Helper interface for values that can be converted into [Error].
 */
public interface ErrorConvertible {
    public fun intoError(): Error
}
