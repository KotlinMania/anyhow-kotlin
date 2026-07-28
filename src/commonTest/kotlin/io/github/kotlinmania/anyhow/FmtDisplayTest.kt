// port-lint: source tests/test_fmt.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

class FmtDisplayTest {
    private fun f(): Result<Unit> = bail(TestIoError("oh no!"))

    private fun g(): Result<Unit> = f().context("f failed")

    private fun h(): Result<Unit> = g().context("g failed")

    @Test
    fun testDisplay() {
        assertEquals("g failed", h().exceptionOrNull()?.toString())
    }

    @Test
    fun testAltDisplay() {
        val errF = f().exceptionOrNull() as Error
        val errG = g().exceptionOrNull() as Error
        val errH = h().exceptionOrNull() as Error

        // Debug what's happening
        println("errF.debugString(true) = '${errF.debugString(true)}'")
        println("errG.debugString(true) = '${errG.debugString(true)}'")
        println("errH.debugString(true) = '${errH.debugString(true)}'")
        
        val chain = errG.chain()
        for (cause in chain) {
            println("chain cause: '${cause}' (${cause::class.simpleName})")
        }
        
        assertEquals("oh no!", errF.debugString(alternate = true))
        assertEquals("f failed: oh no!", errG.debugString(alternate = true))
        assertEquals("g failed: f failed: oh no!", errH.debugString(alternate = true))
    }

    @Test
    fun testDebug() {
        val errF = f().exceptionOrNull() as Error
        val errG = g().exceptionOrNull() as Error
        val errH = h().exceptionOrNull() as Error

        // In Rust, the debug format is just the error + causes (backtrace disabled by default).
        // In Kotlin, backtrace is always captured, so the debug output includes it.
        // We check the error + causes portion (before the backtrace section).
        val debugF = errF.debugString(alternate = false)
        println("debugF = '${debugF}'")
        assertEquals("oh no!", debugF.substringBefore("\n\n"))

        val debugG = errG.debugString(alternate = false)
        println("debugG = '${debugG}'")
        val expectedG = "f failed\n\nCaused by:\n    oh no!"
        assertEquals(expectedG, debugG.substringBefore("\n\n"))

        val debugH = errH.debugString(alternate = false)
        println("debugH = '${debugH}'")
        val expectedH = "g failed\n\nCaused by:\n    0: f failed\n    1: oh no!"
        assertEquals(expectedH, debugH.substringBefore("\n\n"))
    }
}