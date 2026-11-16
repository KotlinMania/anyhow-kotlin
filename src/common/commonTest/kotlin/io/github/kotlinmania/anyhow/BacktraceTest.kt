// port-lint: tests tests/test_backtrace.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BacktraceTest {
    @Test
    fun testBacktrace() {
        val error = anyhow("oh no!")
        val backtrace = error.backtrace()
        assertNotNull(backtrace)
        // In Kotlin, backtrace is always captured via Throwable.stackTraceToString(),
        // so the status is always Captured (never Unsupported like in Rust without
        // RUST_LIB_BACKTRACE set).
        assertEquals(BacktraceStatus.Captured, backtrace.status())
        assertTrue(backtrace.toString().isNotEmpty())
    }
}
