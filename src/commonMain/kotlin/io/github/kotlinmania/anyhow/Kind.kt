@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source kind.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

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

@HiddenFromObjC
public object Adhoc

@HiddenFromObjC
public interface AdhocKind {
    public fun anyhowKind(): Adhoc = Adhoc
}

@HiddenFromObjC
public fun Adhoc.new(message: Any): Error = Error.constructFromAdhoc(message, backtrace())

@HiddenFromObjC
public object Trait

@HiddenFromObjC
public interface TraitKind {
    public fun anyhowKind(): Trait = Trait
}

@HiddenFromObjC
public fun <E : ErrorConvertible> Trait.new(error: E): Error = error.intoError()

@HiddenFromObjC
public interface BoxedKind {
    public fun anyhowKind(): Boxed = Boxed
}

@HiddenFromObjC
public object Boxed

@HiddenFromObjC
public fun Boxed.new(error: StdError): Error {
    val backtrace = backtraceIfAbsent(error)
    return Error.constructFromBoxed(error, backtrace)
}

/**
 * Helper interface for values that can be converted into [Error].
 */
@HiddenFromObjC
public interface ErrorConvertible {
    public fun intoError(): Error
}
