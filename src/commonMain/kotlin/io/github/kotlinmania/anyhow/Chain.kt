// port-lint: source chain.rs
package io.github.kotlinmania.anyhow

public class Chain internal constructor(
    internal var state: ChainState,
) : Iterator<StdError> {
    override fun hasNext(): Boolean = chainHasNext(this)

    override fun next(): StdError = chainNext(this)

    public fun nextBack(): StdError? = chainNextBack(this)

    public fun len(): Int = chainLen(this)

    public fun sizeHint(): Pair<Int, Int?> {
        val len = len()
        return Pair(len, len)
    }

    public fun clone(): Chain = chainClone(this)

    public companion object {
        public fun new(head: StdError): Chain = chainNew(head)

        public fun default(): Chain = chainDefault()
    }
}

internal sealed class ChainState {
    internal data class Linked(
        var next: StdError?,
    ) : ChainState()

    internal data class Buffered(
        val rest: ArrayDeque<StdError>,
    ) : ChainState()
}

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

internal fun chainClone(chain: Chain): Chain = when (val s = chain.state) {
    is ChainState.Linked -> Chain(
        state = ChainState.Linked(next = s.next),
    )
    is ChainState.Buffered -> Chain(
        state = ChainState.Buffered(rest = ArrayDeque<StdError>().also { it.addAll(s.rest) }),
    )
}

internal fun chainDefault(): Chain = Chain(
    state = ChainState.Buffered(rest = ArrayDeque()),
)
