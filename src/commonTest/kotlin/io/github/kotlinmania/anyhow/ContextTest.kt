// port-lint: source tests/test_context.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// https://github.com/dtolnay/anyhow/issues/18
class ContextTest {
    @Test
    fun testInference() {
        val x = "1"
        val result = runCatching { x.toInt() }.context("...")
        assertEquals(1, result.getOrNull())
    }

    private class HighLevel(
        val message: String,
    ) : StdError {
        override fun toString(): String = message
    }

    private class MidLevel(
        val message: String,
    ) : StdError {
        override fun toString(): String = message
    }

    private class LowLevel(
        val message: String,
    ) : StdError {
        override fun toString(): String = message
    }

    private fun makeChain(): Error {
        val low = LowLevel("no such file or directory")
        // Context for a non-Error StdError goes through the anyhow trait path.
        val midError = Error.new(low).context(MidLevel("failed to load config"))
        val high = midError.context(HighLevel("failed to start server"))
        return high
    }

    @Test
    fun testDowncastRef() {
        val err = makeChain()

        assertFalse(err.`is`<String>())
        assertNull(err.downcastRef<String>())

        assertTrue(err.`is`<HighLevel>())
        val high = err.downcastRef<HighLevel>()
        assertNotNull(high)
        assertEquals("failed to start server", high.toString())

        assertTrue(err.`is`<MidLevel>())
        val mid = err.downcastRef<MidLevel>()
        assertNotNull(mid)
        assertEquals("failed to load config", mid.toString())

        assertTrue(err.`is`<LowLevel>())
        val low = err.downcastRef<LowLevel>()
        assertNotNull(low)
        assertEquals("no such file or directory", low.toString())
    }

    @Test
    fun testDowncastHigh() {
        val err = makeChain()
        val result = err.downcast<HighLevel>()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testDowncastMid() {
        val err = makeChain()
        val result = err.downcast<MidLevel>()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testDowncastLow() {
        val err = makeChain()
        val result = err.downcast<LowLevel>()
        assertTrue(result.isSuccess)
    }

    @Test
    fun testUnsuccessfulDowncast() {
        val err = makeChain()
        val result = err.downcast<String>()
        assertTrue(result.isFailure)
    }

    @Test
    fun testRootCause() {
        val err = makeChain()
        assertEquals("no such file or directory", err.rootCause().toString())
    }
}
