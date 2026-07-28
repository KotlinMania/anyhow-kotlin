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

        assertEquals("oh no!", errF.displayString(alternate = true))
        assertEquals("f failed: oh no!", errG.displayString(alternate = true))
        assertEquals("g failed: f failed: oh no!", errH.displayString(alternate = true))
    }

    @Test
    fun testDebug() {
        val errF = f().exceptionOrNull() as Error
        val errG = g().exceptionOrNull() as Error
        val errH = h().exceptionOrNull() as Error

        // Backtrace is always captured in Kotlin, unlike Rust where it is
        // disabled by default in tests. Strip the backtrace portion (which
        // starts at the second "\n\n" separator) to compare only the error
        // and causes section.
        val debugF = errF.debugString(alternate = false)
        assertEquals("oh no!", stripBacktrace(debugF))

        val debugG = errG.debugString(alternate = false)
        val expectedG = "f failed\n\nCaused by:\n    oh no!"
        assertEquals(expectedG, stripBacktrace(debugG))

        val debugH = errH.debugString(alternate = false)
        val expectedH = "g failed\n\nCaused by:\n    0: f failed\n    1: oh no!"
        assertEquals(expectedH, stripBacktrace(debugH))
    }

    // In Rust the debug format does not include a backtrace by default.
    // In Kotlin a backtrace is always captured, so the debug output is
    // "<error and causes>\n\n<backtrace>". This strips the backtrace tail.
    private fun stripBacktrace(s: String): String {
        val lastSeparator = s.lastIndexOf("\n\n")
        return if (lastSeparator >= 0) s.substring(0, lastSeparator) else s
    }
}
