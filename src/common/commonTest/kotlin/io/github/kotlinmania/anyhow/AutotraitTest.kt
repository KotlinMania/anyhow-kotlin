// port-lint: tests tests/test_autotrait.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertTrue

// Rust tests Send, Sync, UnwindSafe, RefUnwindSafe, and Unpin marker traits
// on Error. Kotlin has no equivalent marker trait system — thread safety and
// unwind safety are not expressed as compile-time bounds in Kotlin.
// The portable assertion: Error is a usable type that can be wrapped in
// AnyhowException and caught across boundaries, which is the behavioral analog
// of Send + Sync.
class AutotraitTest {
    @Test
    fun testErrorIsThrowable() {
        val error = anyhow("test error")
        val caught = runCatching { throw AnyhowException(error) }.exceptionOrNull()
        assertTrue(caught is AnyhowException && caught.error.toString() == "test error")
    }
}
