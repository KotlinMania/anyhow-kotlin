// port-lint: tests tests/test_ensure.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// The upstream Rust test_ensure.rs (756 lines) tests the ensure! macro's
// stringification of arbitrarily complex Rust expressions — operator
// precedence, closure syntax, match guards, raw address-of, turbofish,
// pattern destructuring, etc. The Kotlin ensure() is a regular function,
// not a macro; it does not capture or stringify the source expression.
// Only the portable behavioral assertions are ported here; the macro
// stringification tests are Rust-specific and do not translate.
class EnsureTest {
    @Test
    fun testSimpleCondition() {
        assertTrue(ensure(true, "should pass").isSuccess)
        assertTrue(ensure(false, "should fail").isFailure)
    }

    @Test
    fun testErrorMessage() {
        val result = ensure(false, "Condition failed: `false == true && false`")
        assertEquals(
            "Condition failed: `false == true && false`",
            result.exceptionOrNull()?.toString(),
        )
    }

    @Test
    fun testEnsureWithErrorValue() {
        val error = TestIoError("custom error")
        val result = ensure(false, error)
        assertEquals("custom error", result.exceptionOrNull()?.toString())
    }

    @Test
    fun testEnsureWithComparison() {
        val a = 15
        val b = 3
        val result = ensure(a - b <= 10, "Condition failed: `a - b <= 10` (12 vs 10)")
        assertTrue(result.isFailure)
        assertEquals(
            "Condition failed: `a - b <= 10` (12 vs 10)",
            result.exceptionOrNull()?.toString(),
        )
    }
}
