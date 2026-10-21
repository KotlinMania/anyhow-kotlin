package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

internal fun bailLiteral(): Result<Unit, Error> = bail("oh no!")

internal fun bailFmt(): Result<Unit, Error> = bail("${"oh"} ${"no"}!")

internal fun bailError(): Result<Unit, Error> = bail(TestIoError("oh no!"))

internal class TestIoError(
    private val message: String,
) : StdError,
    DebugFormatted {
    override fun toString(): String = message

    override fun debugString(alternate: Boolean): String =
        if (alternate) {
            "Custom {\n    kind: PermissionDenied,\n    error: \"$message\",\n}"
        } else {
            message
        }
}

class CommonTest {
    @Test
    fun testBailLiteral() {
        assertEquals("oh no!", bailLiteral().exceptionOrNull()?.toString())
    }

    @Test
    fun testBailFmt() {
        assertEquals("oh no!", bailFmt().exceptionOrNull()?.toString())
    }

    @Test
    fun testBailError() {
        assertEquals("oh no!", bailError().exceptionOrNull()?.toString())
    }
}
