// port-lint: source tests/test_source.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class IoErrorLike(
    val message: String,
) : StdError {
    override fun toString(): String = message
}

private sealed class TestError : StdError {
    class Io(
        val inner: IoErrorLike,
    ) : TestError() {
        override fun source(): StdError? = inner

        override fun toString(): String = inner.toString()
    }
}

class TestSource {
    @Test
    fun testLiteralSource() {
        val error = anyhow("oh no!")
        assertNull(error.source())
    }

    @Test
    fun testVariableSource() {
        val msg = "oh no!"
        val error = anyhow(msg)
        assertNull(error.source())

        val owned = msg
        val error2 = anyhow(owned)
        assertNull(error2.source())
    }

    @Test
    fun testFmtSource() {
        val error = anyhow("${"oh"} ${"no"}!")
        assertNull(error.source())
    }

    @Test
    fun testIoSource() {
        val io = IoErrorLike("oh no!")
        val error = anyhow(TestError.Io(io))
        assertEquals("oh no!", error.source()?.toString())
    }

    @Test
    fun testAnyhowFromAnyhow() {
        val first = anyhow("oh no!").context("context")
        val error = anyhow(first)
        assertEquals("oh no!", error.source()?.toString())
    }
}
