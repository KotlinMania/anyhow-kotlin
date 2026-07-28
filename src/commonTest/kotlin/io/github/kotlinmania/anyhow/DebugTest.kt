package io.github.kotlinmania.anyhow

import kotlin.test.Test

class DebugTest {
    private fun f(): Result<Unit> = bail(TestIoError("oh no!"))

    private fun g(): Result<Unit> = f().context("f failed")

    @Test
    fun debugTest() {
        val errG = g().exceptionOrNull() as Error
        println("errG class: ${errG::class.simpleName}")
        println("errG.toString(): $errG")

        val stdError = errorImplError(errG.inner.byRef())
        println("stdError class: ${stdError::class.simpleName}")
        println("stdError.toString(): $stdError")

        val cause = stdError.source()
        println("cause: $cause")
        println("cause class: ${cause?.let { it::class.simpleName }}")

        if (cause != null) {
            println("cause.source(): ${cause.source()}")
        }

        val chain = errG.chain()
        for (c in chain) {
            println("chain item: $c (${c::class.simpleName})")
        }

        println("debugString: '${errG.debugString(false)}'")
    }
}
