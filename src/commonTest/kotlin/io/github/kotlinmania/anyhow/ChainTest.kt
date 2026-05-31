// port-lint: source tests/test_chain.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ChainTest {
    private fun error(): Error = anyhow(0).context(1).context(2).context(3)

    @Test
    fun testIter() {
        val chain = error().chain()

        assertEquals("3", chain.next().toString())
        assertEquals("2", chain.next().toString())
        assertEquals("1", chain.next().toString())
        assertEquals("0", chain.next().toString())
        assertFalse(chain.hasNext())
        assertNull(chain.nextBack())
    }

    @Test
    fun testRev() {
        val chain = error().chain()

        assertEquals("0", chain.nextBack()?.toString())
        assertEquals("1", chain.nextBack()?.toString())
        assertEquals("2", chain.nextBack()?.toString())
        assertEquals("3", chain.nextBack()?.toString())
        assertFalse(chain.hasNext())
        assertNull(chain.nextBack())
    }

    @Test
    fun testLen() {
        val chain = error().chain()

        assertEquals(4, chain.len())
        assertEquals(Pair(4, 4), chain.sizeHint())
        assertEquals("3", chain.next().toString())
        assertEquals(3, chain.len())
        assertEquals(Pair(3, 3), chain.sizeHint())
        assertEquals("0", chain.nextBack()?.toString())
        assertEquals(2, chain.len())
        assertEquals(Pair(2, 2), chain.sizeHint())
        assertEquals("2", chain.next().toString())
        assertEquals(1, chain.len())
        assertEquals(Pair(1, 1), chain.sizeHint())
        assertEquals("1", chain.nextBack()?.toString())
        assertEquals(0, chain.len())
        assertEquals(Pair(0, 0), chain.sizeHint())
        assertFalse(chain.hasNext())
    }

    @Test
    fun testDefault() {
        val chain = Chain.default()

        assertFalse(chain.hasNext())
    }

    @Test
    fun testClone() {
        val chain = error().chain().clone()

        assertEquals("3", chain.next().toString())
        assertEquals("2", chain.next().toString())
        assertEquals("1", chain.next().toString())
        assertEquals("0", chain.next().toString())
        assertFalse(chain.hasNext())
        assertNull(chain.nextBack())
    }
}
