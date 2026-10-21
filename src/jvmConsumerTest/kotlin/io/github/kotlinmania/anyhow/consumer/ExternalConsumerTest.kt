package io.github.kotlinmania.anyhow.consumer

import io.github.kotlinmania.anyhow.Error
import io.github.kotlinmania.anyhow.StdError
import io.github.kotlinmania.anyhow.anyhow
import io.github.kotlinmania.anyhow.context
import io.github.kotlinmania.anyhow.debugString
import io.github.kotlinmania.anyhow.displayString
import io.github.kotlinmania.anyhow.downcastRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * An independent external error implementation compiled outside friend paths.
 *
 * Verifies that external consumers implementing [StdError] can provide custom
 * Display (`toString`) and Debug (`debugString`) representations and have them
 * preserved through [anyhow], downcasting, and contextual source wrapping without
 * requiring friend visibility.
 */
class ExternalCustomError(
    val code: Int,
    val message: String,
) : StdError {
    override fun source(): StdError? = null

    // Deliberately different Display vs Debug output
    override fun toString(): String = "Error $code: $message"

    override fun debugString(alternate: Boolean): String =
        if (alternate) {
            "ExternalCustomError {\n    code: $code,\n    message: \"$message\",\n}"
        } else {
            "ExternalCustomError($code, \"$message\")"
        }
}

class ExternalConsumerTest {
    private fun stripBacktrace(s: String): String {
        val causedBy = s.indexOf("Caused by:")
        if (causedBy < 0) {
            val doubleNewline = s.indexOf("\n\n")
            return if (doubleNewline >= 0) s.substring(0, doubleNewline) else s
        }

        val afterCausedBy = s.indexOf("\n\n", causedBy)
        return if (afterCausedBy >= 0) s.substring(0, afterCausedBy) else s
    }

    @Test
    fun testExternalErrorFormattingAndObjectIdentity() {
        val external = ExternalCustomError(404, "resource not found")
        val err: Error = anyhow(external)

        // 1. Exact object identity downcast
        val downcasted = err.downcastRef<ExternalCustomError>()
        assertNotNull(downcasted)
        assertSame(external, downcasted)

        // 2. Normal Display (outermost Display)
        assertEquals("Error 404: resource not found", err.displayString(alternate = false))

        // 3. Normal Debug (Display + backtrace)
        assertEquals("Error 404: resource not found", stripBacktrace(err.debugString(alternate = false)))

        // 4. Alternate Debug (underlying Debug::fmt)
        val expectedAltDebug =
            "ExternalCustomError {\n" +
                "    code: 404,\n" +
                "    message: \"resource not found\",\n" +
                "}"
        assertEquals(expectedAltDebug, err.debugString(alternate = true))
    }

    @Test
    fun testExternalErrorContextualFormattingAndIdentityPreservation() {
        val external = ExternalCustomError(500, "database connection failed")
        val err: Error = anyhow(external)
        val withContext: Error = err.context("request processing failed")

        // 1. Exact object identity preserved through context
        val downcasted = withContext.downcastRef<ExternalCustomError>()
        assertNotNull(downcasted)
        assertSame(external, downcasted)

        // 2. Context Display (outer context)
        assertEquals("request processing failed", withContext.displayString(alternate = false))

        // 3. Context Alternate Display (outer: inner)
        assertEquals(
            "request processing failed: Error 500: database connection failed",
            withContext.displayString(alternate = true),
        )

        // 4. Context Normal Debug (chain)
        val contextDebug = withContext.debugString(alternate = false)
        assertTrue(contextDebug.contains("request processing failed"))
        assertTrue(contextDebug.contains("Caused by:"))
        assertTrue(contextDebug.contains("Error 500: database connection failed"))

        // 5. Context Alternate Debug (recursively formatted struct with 4-space indent)
        val expectedContextAltDebug =
            "Error {\n" +
                "    context: \"request processing failed\",\n" +
                "    source: ExternalCustomError {\n" +
                "        code: 500,\n" +
                "        message: \"database connection failed\",\n" +
                "    },\n" +
                "}"
        assertEquals(expectedContextAltDebug, withContext.debugString(alternate = true))

        // 6. Double context layer
        val withDoubleContext: Error = withContext.context("gateway dispatch failed")
        val downcastedDouble = withDoubleContext.downcastRef<ExternalCustomError>()
        assertNotNull(downcastedDouble)
        assertSame(external, downcastedDouble)

        val expectedDoubleAltDebug =
            "Error {\n" +
                "    context: \"gateway dispatch failed\",\n" +
                "    source: Error {\n" +
                "        context: \"request processing failed\",\n" +
                "        source: ExternalCustomError {\n" +
                "            code: 500,\n" +
                "            message: \"database connection failed\",\n" +
                "        },\n" +
                "    },\n" +
                "}"
        assertEquals(expectedDoubleAltDebug, withDoubleContext.debugString(alternate = true))
    }
}
