@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source chain.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

internal typealias Item = StdError

/**
 * Iterator of a chain of source errors.
 *
 * This type is the iterator returned by [Error.chain].
 *
 * # Example
 *
 * ```kotlin
 * fun underlyingIoErrorKind(error: Error): IoErrorKind? {
 *     for (cause in error.chain()) {
 *         if (cause is IoError) {
 *             return cause.kind()
 *         }
 *     }
 *     return null
 * }
 * ```
 */
@HiddenFromObjC
public class Chain internal constructor(
    internal var state: ChainState,
) : Iterator<StdError> {
    override fun hasNext(): Boolean =
        when (val s = state) {
            is ChainState.Linked -> s.next != null
            is ChainState.Buffered -> s.rest.isNotEmpty()
        }

    override fun next(): Item = next(this)

    public companion object
}

internal fun next(chain: Chain): Item =
    when (val s = chain.state) {
        is ChainState.Linked -> {
            val error = checkNotNull(s.next)
            s.next = error.source()
            error
        }

        is ChainState.Buffered -> {
            s.rest.removeFirst()
        }
    }

@HiddenFromObjC
public fun Chain.Companion.new(head: StdError): Chain =
    Chain(
        state = ChainState.Linked(next = head),
    )

@HiddenFromObjC
public fun Chain.sizeHint(): Pair<Int, Int?> {
    val len = len()
    return Pair(len, len)
}

@HiddenFromObjC
public fun Chain.nextBack(): Item? =
    when (val s = state) {
        is ChainState.Linked -> {
            val rest = ArrayDeque<StdError>()
            var next: StdError? = s.next
            while (next != null) {
                val cause = next
                next = cause.source()
                rest.addLast(cause)
            }
            val last = rest.removeLastOrNull()
            state = ChainState.Buffered(rest = rest)
            last
        }

        is ChainState.Buffered -> {
            s.rest.removeLastOrNull()
        }
    }

@HiddenFromObjC
public fun Chain.len(): Int =
    when (val s = state) {
        is ChainState.Linked -> {
            var len = 0
            var next: StdError? = s.next
            while (next != null) {
                val cause = next
                next = cause.source()
                len += 1
            }
            len
        }

        is ChainState.Buffered -> {
            s.rest.size
        }
    }

@HiddenFromObjC
public fun Chain.clone(): Chain =
    when (val s = state) {
        is ChainState.Linked -> {
            Chain(
                state = ChainState.Linked(next = s.next),
            )
        }

        is ChainState.Buffered -> {
            Chain(
                state = ChainState.Buffered(rest = ArrayDeque<StdError>().also { it.addAll(s.rest) }),
            )
        }
    }

@HiddenFromObjC
public fun Chain.Companion.default(): Chain =
    Chain(
        state = ChainState.Buffered(rest = ArrayDeque()),
    )

internal sealed class ChainState {
    internal data class Linked(
        var next: StdError?,
    ) : ChainState()

    internal data class Buffered(
        val rest: ArrayDeque<StdError>,
    ) : ChainState()
}
