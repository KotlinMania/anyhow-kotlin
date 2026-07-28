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
    // The backtrace section is the last "\n\n"-separated block that is NOT
    // part of the "Caused by:" cause chain. The cause chain lines are indented
    // (start with spaces) or are the "Caused by:" header; the backtrace starts
    // with a non-indented line.
    private fun stripBacktrace(s: String): String {
        val causedByIndex = s.indexOf("Caused by:")
        if (causedByIndex == -1) {
            // No cause chain — the backtrace (if any) follows the error message.
            val separator = s.indexOf("\n\n")
            return if (separator >= 0) s.substring(0, separator) else s
        }
        // After "Caused by:", find the backtrace section: it starts with "\n\n"
        // followed by a non-indented line (not starting with a space or digit).
        val afterCausedBy = causedByIndex + "Caused by:".length
        val rest = s.substring(afterCausedBy)
        var searchFrom = 0
        while (true) {
            val nextSeparator = rest.indexOf("\n\n", searchFrom)
            if (nextSeparator == -1) return s
            val afterSeparator = nextSeparator + 2
            val lineStart = rest.substring(afterSeparator)
            // Cause lines start with spaces (indented) or are empty;
            // backtrace lines start with a non-whitespace character.
            if (lineStart.isNotEmpty() && !lineStart.startsWith(" ") && !lineStart.startsWith("\t")) {
                return s.substring(0, afterCausedBy + nextSeparator)
            }
            searchFrom = nextSeparator + 1
        }
    }
}
