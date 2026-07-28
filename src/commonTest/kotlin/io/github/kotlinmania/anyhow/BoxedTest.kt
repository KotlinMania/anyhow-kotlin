// port-lint: source tests/test_boxed.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BoxedTest {
    @Test
    fun testBoxedStr() {
        val error = anyhow("oh no!")
        assertEquals("oh no!", error.toString())
        val downcast = error.downcastRef<String>()
        assertNotNull(downcast)
        assertEquals("oh no!", downcast)
    }

    @Test
    fun testBoxedAnyhow() {
        val error = anyhow("oh no!").context("it failed")
        val wrapped = anyhow(error)
        val source = wrapped.source()
        assertNotNull(source)
        assertEquals("oh no!", source.toString())
    }
}