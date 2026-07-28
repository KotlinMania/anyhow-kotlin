// port-lint: source tests/test_convert.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

class ConvertTest {
    @Test
    fun testConvert() {
        val error = Error.new(TestIoError("oh no!"))
        assertEquals("oh no!", error.toString())
    }

    @Test
    fun testConvertSend() {
        val error = Error.new(TestIoError("oh no!"))
        assertEquals("oh no!", error.toString())
    }

    @Test
    fun testConvertSendSync() {
        val error = Error.new(TestIoError("oh no!"))
        assertEquals("oh no!", error.toString())
    }

    @Test
    fun testQuestionMark() {
        val f = { Ok(Unit) }
        val result = f()
        assertEquals(Unit, result.getOrNull())
    }
}