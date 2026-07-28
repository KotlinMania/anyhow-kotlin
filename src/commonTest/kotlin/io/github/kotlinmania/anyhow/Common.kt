// port-lint: source tests/common/mod.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal fun bailLiteral(): Result<Unit> = bail("oh no!")

internal fun bailFmt(): Result<Unit> = bail("${"oh"} ${"no"}!")

internal fun bailError(): Result<Unit> = bail(TestIoError("oh no!"))

internal class TestIoError(
    private val message: String,
) : StdError {
    override fun toString(): String = message
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