// port-lint: source tests/test_downcast.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DowncastTest {
    @Test
    fun testDowncast() {
        val errLiteral = bailLiteral().exceptionOrNull() as Error
        val result = errLiteral.downcast<String>()
        assertTrue(result.isSuccess)
        assertEquals("oh no!", result.getOrNull())

        val errFmt = bailFmt().exceptionOrNull() as Error
        val resultFmt = errFmt.downcast<String>()
        assertTrue(resultFmt.isSuccess)
        assertEquals("oh no!", resultFmt.getOrNull())

        val errError = bailError().exceptionOrNull() as Error
        val resultError = errError.downcast<TestIoError>()
        assertTrue(resultError.isSuccess)
        assertEquals("oh no!", resultError.getOrNull()?.toString())
    }

    @Test
    fun testDowncastRef() {
        val errLiteral = bailLiteral().exceptionOrNull() as Error
        assertEquals("oh no!", errLiteral.downcastRef<String>())

        val errFmt = bailFmt().exceptionOrNull() as Error
        assertEquals("oh no!", errFmt.downcastRef<String>())

        val errError = bailError().exceptionOrNull() as Error
        assertEquals("oh no!", errError.downcastRef<TestIoError>()?.toString())
    }

    @Test
    fun testDowncastMut() {
        val errLiteral = bailLiteral().exceptionOrNull() as Error
        assertEquals("oh no!", errLiteral.downcastMut<String>())

        val errFmt = bailFmt().exceptionOrNull() as Error
        assertEquals("oh no!", errFmt.downcastMut<String>())

        val errError = bailError().exceptionOrNull() as Error
        assertEquals("oh no!", errError.downcastMut<TestIoError>()?.toString())
    }

    @Test
    fun testIs() {
        val errLiteral = bailLiteral().exceptionOrNull() as Error
        assertTrue(errLiteral.`is`<String>())
        assertFalse(errLiteral.`is`<Int>())
    }

    @Test
    fun testAsRef() {
        val error = bailError().exceptionOrNull() as Error
        assertEquals("oh no!", error.toString())
    }
}
