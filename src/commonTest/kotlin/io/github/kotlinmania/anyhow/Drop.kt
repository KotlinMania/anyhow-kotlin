// port-lint: source tests/drop/mod.rs
package io.github.kotlinmania.anyhow

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.test.assertTrue

/**
 * Shared flag that tracks whether the associated [DetectDrop] has been
 * closed. Kotlin has no deterministic Drop trait; the test uses
 * [AutoCloseable] and an explicit close call to simulate the Rust
 * Drop semantics.
 */
internal class Flag {
    private val atomic = AtomicBoolean(false)

    fun get(): Boolean = atomic.load()

    fun set() = atomic.store(true)
}

/**
 * A [StdError] that sets its [Flag] when [close] is called.
 * In Rust, Drop runs automatically; here the caller must call [close]
 * to trigger the same effect.
 */
internal class DetectDrop(
    private val hasDropped: Flag,
) : StdError, AutoCloseable {
    override fun toString(): String = "oh no!"

    override fun close() {
        hasDropped.set()
    }
}