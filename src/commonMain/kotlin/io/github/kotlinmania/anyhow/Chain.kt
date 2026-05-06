// port-lint: source src/chain.rs
package io.github.kotlinmania.anyhow

/**
 * Internal state for the [Chain] iterator.
 *
 * The upstream includes multiple conditional compilation branches for `std` and `no_std` builds.
 * Kotlin has no equivalent conditional compilation in the language; this port keeps a single
 * representation that supports forward iteration and the "double-ended" behavior through a
 * buffered deque.
 */
internal sealed class ChainState {
    internal data class Linked(
        var next: StdError?,
    ) : ChainState()

    internal data class Buffered(
        val rest: ArrayDeque<StdError>,
    ) : ChainState()
}

/**
 * Create a new chain iterator starting at [head].
 *
 * Corresponds to `Chain::new(head)` in the upstream.
 */
public fun chainNew(head: StdError): Chain = Chain(
    state = ChainState.Linked(next = head),
)

internal fun chainHasNext(chain: Chain): Boolean = when (val s = chain.state) {
    is ChainState.Linked -> s.next != null
    is ChainState.Buffered -> s.rest.isNotEmpty()
}

internal fun chainNext(chain: Chain): StdError {
    when (val s = chain.state) {
        is ChainState.Linked -> {
            val error = s.next ?: throw NoSuchElementException()
            s.next = error.source()
            return error
        }
        is ChainState.Buffered -> {
            val next = s.rest.removeFirstOrNull()
            return next ?: throw NoSuchElementException()
        }
    }
}

/**
 * Kotlin analog of the upstream `DoubleEndedIterator::next_back`.
 *
 * When the chain is still in the linked state, we buffer the rest of the causes and switch into
 * a buffered representation, then yield from the back.
 */
internal fun chainNextBack(chain: Chain): StdError? {
    return when (val s = chain.state) {
        is ChainState.Linked -> {
            val rest = ArrayDeque<StdError>()
            var next: StdError? = s.next
            while (next != null) {
                rest.addLast(next)
                next = next.source()
            }
            val last = rest.removeLastOrNull()
            chain.state = ChainState.Buffered(rest = rest)
            last
        }
        is ChainState.Buffered -> s.rest.removeLastOrNull()
    }
}

/**
 * Kotlin analog of the upstream `ExactSizeIterator::len`.
 */
internal fun chainLen(chain: Chain): Int = when (val s = chain.state) {
    is ChainState.Linked -> {
        var len = 0
        var next: StdError? = s.next
        while (next != null) {
            len += 1
            next = next.source()
        }
        len
    }
    is ChainState.Buffered -> s.rest.size
}

/**
 * Kotlin analog of the upstream `Default for Chain`.
 */
internal fun chainDefault(): Chain = Chain(
    state = ChainState.Buffered(rest = ArrayDeque()),
)

