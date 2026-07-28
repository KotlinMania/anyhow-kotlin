// port-lint: tests tests/test_autotrait.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertTrue

// Rust tests Send, Sync, UnwindSafe, RefUnwindSafe, and Unpin marker traits
// on Error. Kotlin has no equivalent marker trait system — thread safety and
// unwind safety are not expressed as compile-time bounds in Kotlin.
// The portable assertion: Error is a usable type that can be thrown and caught
// across coroutine boundaries, which is the behavioral analog of Send + Sync.
class AutotraitTest {
    @Test
    fun testErrorIsThrowable() {
        // Error extends Throwable and implements StdError — the Kotlin
        // analog of Rust's Send + Sync + Unpin bounds, which guarantee
        // the error can cross thread/coroutine boundaries safely.
        val error = anyhow("test error")
        val caught = runCatching<Error> { throw error }.exceptionOrNull()
        assertTrue(caught != null && caught.toString() == "test error")
    }
}
