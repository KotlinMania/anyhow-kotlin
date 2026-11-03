// port-lint: source tests/test_macros.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacrosTest {
    @Test
    fun testMessages() {
        assertEquals("oh no!", bailLiteral().exceptionOrNull()?.toString())
        assertEquals("oh no!", bailFmt().exceptionOrNull()?.toString())
        assertEquals("oh no!", bailError().exceptionOrNull()?.toString())
    }

    @Test
    fun testEnsure() {
        val f1 = { ensure(1 + 1 == 2, "This is correct") }
        assertTrue(f1().isSuccess)

        val v = 1
        val f2 = { ensure(v + v == 2, "This is correct, v: $v") }
        assertTrue(f2().isSuccess)

        val f3 = { ensure(v + v == 1, "This is not correct, v: $v") }
        assertTrue(f3().isFailure)

        val f4 = { ensure(v + v == 1, "Condition failed: `v + v == 1` (2 vs 1)") }
        assertEquals(
            "Condition failed: `v + v == 1` (2 vs 1)",
            f4().exceptionOrNull()?.toString(),
        )
    }

    @Test
    fun testBraceEscape() {
        val err = anyhow("unterminated \${..} expression")
        assertEquals("unterminated \${..} expression", err.toString())
    }
}
