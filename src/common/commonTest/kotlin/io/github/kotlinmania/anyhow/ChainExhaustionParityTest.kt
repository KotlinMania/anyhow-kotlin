package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ChainExhaustionParityTest {
    private class Node(
        private val label: String,
        private val cause: StdError? = null,
    ) : StdError {
        var calls = 0

        override fun source(): StdError? {
            calls++
            return cause
        }

        override fun toString(): String = label
    }

    @Test
    fun linkedIteratorExhaustionUsesTheKotlinIteratorException() {
        val node = Node("only")
        val chain = Chain.new(node)
        assertEquals("only", chain.next().toString())
        repeat(2) { assertFailsWith<NoSuchElementException> { chain.next() } }
        assertEquals(1, node.calls)
    }

    @Test
    fun bufferedIteratorExhaustionUsesTheSameException() {
        val node = Node("only")
        val chain = Chain.new(node)
        chain.nextBack()
        assertFailsWith<NoSuchElementException> { chain.next() }
        assertFailsWith<NoSuchElementException> { Chain.default().next() }
    }

    @Test
    fun nullableAdvanceMatchesStableSourceExhaustionInBothStates() {
        val linkedNode = Node("linked")
        val linked = Chain.new(linkedNode)
        assertSame(linkedNode, linked.nextOrNull())
        repeat(2) { assertNull(linked.nextOrNull()) }
        assertEquals(1, linkedNode.calls)
        val buffered = Chain.new(Node("buffered"))
        buffered.nextBack()
        repeat(2) { assertNull(buffered.nextOrNull()) }
        assertNull(Chain.default().nextOrNull())
    }

    @Test
    fun mixedEndsAndSizeHintsMatchSource() {
        val tail = Node("tail")
        val middle = Node("middle", tail)
        val head = Node("head", middle)
        val chain = Chain.new(head)
        assertEquals(SizeHint(3, 3), chain.sizeHint())
        assertSame(head, chain.nextOrNull())
        assertSame(tail, chain.nextBack())
        assertEquals(SizeHint(1, 1), chain.sizeHint())
        assertSame(middle, chain.nextOrNull())
        assertNull(chain.nextOrNull())
        assertEquals(0, chain.len())
    }

    @Test
    fun clonesShareErrorsButKeepIndependentIteratorState() {
        val tail = Node("tail")
        val middle = Node("middle", tail)
        val head = Node("head", middle)
        val chain = Chain.new(head)
        val clone = chain.clone()
        assertSame(chain.nextOrNull(), clone.nextOrNull())
        assertSame(tail, chain.nextBack())
        val bufferedClone = chain.clone()
        assertSame(chain.nextOrNull(), bufferedClone.nextOrNull())
        assertNull(chain.nextOrNull())
        assertSame(middle, clone.nextOrNull())
    }

    @Test
    fun nullableAdvanceDoesNotPrecomputeTheRemainingChain() {
        val middle = Node("middle", Node("tail"))
        val head = Node("head", middle)
        val chain = Chain.new(head)
        assertEquals(0, head.calls)
        assertSame(head, chain.nextOrNull())
        assertEquals(1, head.calls)
        assertEquals(0, middle.calls)
    }

    @Test
    fun sourceFailurePropagatesWithoutLosingTheCurrentItem() {
        val failure = IllegalArgumentException("source failure")
        var fail = true
        val node =
            object : StdError {
                override fun source(): StdError? {
                    if (fail) {
                        fail = false
                        throw failure
                    }
                    return null
                }
            }
        val chain = Chain.new(node)
        assertSame(failure, assertFailsWith<IllegalArgumentException> { chain.nextOrNull() })
        assertSame(node, chain.nextOrNull())
        assertNull(chain.nextOrNull())
    }
}
