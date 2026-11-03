// port-lint: source tests/test_boxed.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import io.github.kotlinmania.thiserror.StdError as ThiserrorStdError

class BoxedTest {
    @Test
    fun testBoxedStr() {
        val error = anyhow("oh no!")
        assertEquals("oh no!", error.toString())
        val downcast = error.downcastRef<String>()
        assertNotNull(downcast)
        assertEquals("oh no!", downcast)
    }

    private class MyError(
        val source: ThiserrorStdError,
    ) : ThiserrorStdError {
        override fun source(): ThiserrorStdError = source

        override fun toString(): String = "outer"
    }

    private fun anyhow(error: ThiserrorStdError): Error =
        anyhow(
            object : StdError {
                override fun source(): StdError? =
                    error.source()?.let { src ->
                        object : StdError {
                            override fun toString(): String = src.toString()
                        }
                    }

                override fun toString(): String = error.toString()
            },
        )

    @Test
    fun testBoxedThiserror() {
        val ioError =
            object : ThiserrorStdError {
                override fun toString(): String = "oh no!"
            }
        val error = MyError(source = ioError)
        val wrapped = anyhow(error)
        val source = wrapped.source()
        assertNotNull(source)
        assertEquals("oh no!", source.toString())
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
