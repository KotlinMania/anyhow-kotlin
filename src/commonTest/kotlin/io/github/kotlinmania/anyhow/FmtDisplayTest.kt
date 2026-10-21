// port-lint: source tests/test_fmt.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

class FmtDisplayTest {
    companion object {
        private const val EXPECTED_ALTDEBUG_F: String =
            "Custom {\n" +
                "    kind: PermissionDenied,\n" +
                "    error: \"oh no!\",\n" +
                "}"

        private const val EXPECTED_ALTDEBUG_G: String =
            "Error {\n" +
                "    context: \"f failed\",\n" +
                "    source: Custom {\n" +
                "        kind: PermissionDenied,\n" +
                "        error: \"oh no!\",\n" +
                "    },\n" +
                "}"

        private const val EXPECTED_ALTDEBUG_H: String =
            "Error {\n" +
                "    context: \"g failed\",\n" +
                "    source: Error {\n" +
                "        context: \"f failed\",\n" +
                "        source: Custom {\n" +
                "            kind: PermissionDenied,\n" +
                "            error: \"oh no!\",\n" +
                "        },\n" +
                "    },\n" +
                "}"
    }

    private fun f(): Result<Unit, Error> = bail(TestIoError("oh no!"))

    private fun g(): Result<Unit, Error> = f().context("f failed")

    private fun h(): Result<Unit, Error> = g().context("g failed")

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

        val debugF = errF.debugString(alternate = false)
        assertEquals("oh no!", stripBacktrace(debugF))

        val debugG = errG.debugString(alternate = false)
        val expectedG = "f failed\n\nCaused by:\n    oh no!"
        assertEquals(expectedG, stripBacktrace(debugG))

        val debugH = errH.debugString(alternate = false)
        val expectedH = "g failed\n\nCaused by:\n    0: f failed\n    1: oh no!"
        assertEquals(expectedH, stripBacktrace(debugH))
    }

    @Test
    fun testAltDebug() {
        val errF = f().exceptionOrNull() as Error
        val errG = g().exceptionOrNull() as Error
        val errH = h().exceptionOrNull() as Error

        assertEquals(EXPECTED_ALTDEBUG_F, errF.debugString(alternate = true))
        assertEquals(EXPECTED_ALTDEBUG_G, errG.debugString(alternate = true))
        assertEquals(EXPECTED_ALTDEBUG_H, errH.debugString(alternate = true))
    }

    private fun stripBacktrace(s: String): String {
        val causedBy = s.indexOf("Caused by:")
        if (causedBy < 0) {
            val doubleNewline = s.indexOf("\n\n")
            return if (doubleNewline >= 0) s.substring(0, doubleNewline) else s
        }

        val afterCausedBy = s.indexOf("\n\n", causedBy)
        return if (afterCausedBy >= 0) s.substring(0, afterCausedBy) else s
    }
}
