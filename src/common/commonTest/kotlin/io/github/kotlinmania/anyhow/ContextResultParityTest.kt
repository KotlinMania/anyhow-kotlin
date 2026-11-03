package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class ContextResultParityTest {
    @Test
    fun nullableSuccessPreservesItsValueWithoutEvaluatingContext() {
        var calls = 0
        val input = kotlin.Result.success<String?>(null)
        val result =
            input.withContext {
                calls++
                "unused"
            }
        assertIs<Result.Ok<String?>>(result)
        assertNull(result.getOrThrow())
        assertEquals(0, calls)
        assertNull(input.context("unused").getOrThrow())
    }

    @Test
    fun failureRunsSupplierOnceAndPreservesTheActualThrowable() {
        val failure = IllegalArgumentException("original")
        var calls = 0
        val input = kotlin.Result.failure<Unit>(failure)
        val contextual =
            input.withContext {
                calls++
                "outer"
            }
        val error = contextual.exceptionOrNull()!!
        assertEquals(1, calls)
        assertEquals("outer", error.toString())
        assertSame(failure, assertIs<ThrowableStdError>(error.rootCause()).value)
        val eager = input.context("outer").exceptionOrNull()!!
        assertSame(failure, assertIs<ThrowableStdError>(eager.rootCause()).value)
    }

    @Test
    fun supplierFailurePropagatesInsteadOfBecomingAnErrorValue() {
        val failure = IllegalArgumentException("context failure")
        val input = kotlin.Result.failure<Unit>(IllegalStateException("original"))
        assertSame(failure, assertFailsWith<IllegalArgumentException> { input.withContext { throw failure } })
    }
}
