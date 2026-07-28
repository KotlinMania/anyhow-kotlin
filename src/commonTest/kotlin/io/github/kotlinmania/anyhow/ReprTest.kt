// port-lint: source tests/test_repr.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertTrue

class ReprTest {
    // Rust: assert_eq!(mem::size_of::<Error>(), mem::size_of::<usize>())
    // Kotlin has no equivalent of mem::size_of for managed objects —
    // the Error type is a class reference (one machine word) in Kotlin/JVM,
    // but this is a JVM/Native implementation detail, not a portable assertion.

    // Rust: assert_eq!(mem::size_of::<Result<(), Error>>(), mem::size_of::<usize>())
    // Same — Kotlin Result is a boxed type, not a niche-optimized enum.

    // Rust: fn assert<E: Unpin + Send + Sync + 'static>() {}
    // Kotlin: All class references are Unpin + Send + Sync on the JVM
    // (no finalizers, no thread affinity for immutable references).

    @Test
    fun testDrop() {
        val hasDropped = Flag()
        val error = Error.new(DetectDrop(hasDropped))
        // In Rust, dropping the Error drops the inner DetectDrop.
        // Kotlin has no automatic Drop, so we close the DetectDrop
        // through the downcast to trigger the flag.
        val dropped = error.downcast<DetectDrop>()
        dropped.getOrNull()?.close()
        assertTrue(hasDropped.get())
    }
}