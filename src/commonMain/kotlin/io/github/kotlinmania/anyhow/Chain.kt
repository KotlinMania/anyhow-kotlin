// port-lint: source chain.rs

package io.github.kotlinmania.anyhow

internal typealias Item = StdError

/**
 * Bounds on the remaining length of an iterator.
 */
public data class SizeHint(
    public val lower: Int,
    public val upper: Int?,
)

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
public class Chain internal constructor(
    internal var state: ChainState,
) : Iterator<StdError> {
    override fun hasNext(): Boolean =
        when (val s = state) {
            is ChainState.Linked -> s.next != null
            is ChainState.Buffered -> s.rest.isNotEmpty()
        }

    override fun next(): Item = next(this) ?: throw NoSuchElementException()

    /** Returns the next source error, or null once the chain is exhausted. */
    public fun nextOrNull(): StdError? = next(this)

    public companion object
}

internal fun next(chain: Chain): Item? =
    when (val s = chain.state) {
        is ChainState.Linked -> {
            val error = s.next
            if (error != null) s.next = error.source()
            error
        }

        is ChainState.Buffered -> {
            s.rest.removeFirstOrNull()
        }
    }

public fun Chain.Companion.new(head: StdError): Chain =
    Chain(
        state = ChainState.Linked(next = head),
    )

public fun Chain.sizeHint(): SizeHint {
    val len = len()
    return SizeHint(len, len)
}

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
