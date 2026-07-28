// port-lint: tests tests/test_ffi.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// The upstream Rust test_ffi.rs tests #[no_mangle] extern "C" functions that
// pass anyhow::Error across the FFI boundary. Kotlin/Native has its own
// interop mechanism (cinterop, @CName), and #[no_mangle]/extern "C" are
// Rust-specific ABI annotations with no direct Kotlin equivalent.
// The portable assertion: an Error can be constructed, boxed, and unboxed
// through the anyhow wrapper — which is the behavioral analog of what
// the FFI tests exercise (Error remains valid when moved across boundaries).
class FfiTest {
    @Test
    fun testErrorRoundTrip() {
        val error = anyhow("ffi error")
        val boxed = error.intoBoxedDynError()
        assertNotNull(boxed)
        assertEquals("ffi error", boxed.toString())
    }

    @Test
    fun testErrorFromOption() {
        val error = anyhow("ffi error")
        val result = Result.failure<Error>(error)
        val recovered = result.exceptionOrNull() as Error
        assertEquals("ffi error", recovered.toString())
    }
}
