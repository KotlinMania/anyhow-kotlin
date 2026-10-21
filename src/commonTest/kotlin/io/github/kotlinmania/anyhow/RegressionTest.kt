package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RegressionTest {
    // 1. Successful null: Ok<String?>(null) must be recognized as success,
    // not rejected by a nullable-field XOR invariant.
    @Test
    fun testNullableSuccess() {
        val res: Result<String?, Error> = Ok<String?>(null)
        assertTrue(res.isSuccess, "Nullable success must have isSuccess == true")
        assertFalse(res.isFailure, "Nullable success must have isFailure == false")
        assertNull(res.getOrNull(), "getOrNull() on Ok(null) must be null")
        assertNull(res.exceptionOrNull(), "exceptionOrNull() on Ok(null) must be null")
        assertNull(res.getOrThrow(), "getOrThrow() on Ok(null) must return null without throwing")
        val ok = res as? Result.Ok
        assertNotNull(ok, "res must be instance of Result.Ok")
        assertNull(ok.value)
    }

    // 2. Custom E in generic Result contract: Result<T, E> can hold an arbitrary
    // non-Throwable, non-StdError error type E.
    private data class CustomDiagnostic(
        val code: Int,
        val message: String,
    )

    @Test
    fun testCustomErrorTypeInResult() {
        val diag = CustomDiagnostic(404, "resource missing")
        val res: Result<String, CustomDiagnostic> = Result.err(diag)

        assertFalse(res.isSuccess)
        assertTrue(res.isFailure)
        assertNull(res.getOrNull())
        assertNotNull(res.exceptionOrNull())
        assertSame(diag, res.exceptionOrNull())
        val err = res as? Result.Err
        assertNotNull(err, "res must be instance of Result.Err")
        assertSame(diag, err.error)
    }

    // 3. Context / cause / downcast object identity and causal ordering.
    private class RootCause(
        val msg: String,
    ) : StdError {
        override fun toString(): String = msg
    }

    private class MidCause(
        val msg: String,
    ) : StdError {
        override fun toString(): String = msg
    }

    private class OuterContext(
        val msg: String,
    ) {
        override fun toString(): String = msg
    }

    @Test
    fun testContextChainOrderingAndDowncastIdentity() {
        val root = RootCause("root failure")
        val mid = MidCause("mid failure")
        val outer = OuterContext("outer context")

        val err = Error.new(root).context(mid).context(outer)

        // Traversal order: outermost first, then down to root.
        val chain = err.chain()
        val chainList = mutableListOf<StdError>()
        while (chain.hasNext()) {
            chainList.add(chain.next())
        }
        assertEquals(3, chainList.size)
        assertEquals("outer context", chainList[0].toString())
        assertEquals("mid failure", chainList[1].toString())
        assertEquals("root failure", chainList[2].toString())

        // Root cause matches the deepest element
        assertEquals("root failure", err.rootCause().toString())

        // Downcast by reference preserves identity
        val downcastOuter = err.downcastRef<OuterContext>()
        assertNotNull(downcastOuter)
        assertSame(outer, downcastOuter)

        val downcastMid = err.downcastRef<MidCause>()
        assertNotNull(downcastMid)
        assertSame(mid, downcastMid)

        val downcastRoot = err.downcastRef<RootCause>()
        assertNotNull(downcastRoot)
        assertSame(root, downcastRoot)
    }

    // 4. Display versus Debug formatting contract.
    @Test
    fun testDisplayVsDebug() {
        val root = RootCause("connection refused")
        val err = Error.new(root).context("failed to connect to host")

        // Display (non-alternate) renders only outermost context
        assertEquals("failed to connect to host", err.displayString(alternate = false))

        // Display (alternate) renders outermost: cause
        assertEquals("failed to connect to host: connection refused", err.displayString(alternate = true))

        // Debug renders full causal chain with backtrace header
        val debug = err.debugString(alternate = false)
        assertTrue(debug.contains("failed to connect to host"), "Debug must contain context")
        assertTrue(debug.contains("Caused by:"), "Debug must contain Caused by:")
        assertTrue(debug.contains("connection refused"), "Debug must contain root cause")
    }

    // 5. Result.context() bound and cause preservation:
    // Calling .context() on a Result.Err(StdError) attaches outer context while preserving
    // the inner error in the cause chain without discarding its representation.
    @Test
    fun testResultContextPreservesInnerError() {
        val root = RootCause("inner failure")
        val errResult: Result<String, RootCause> = Result.err(root)
        val withCtx: Result<String, Error> = errResult.context("higher level step")

        assertTrue(withCtx.isFailure)
        val outerErr = withCtx.exceptionOrNull()
        assertNotNull(outerErr)
        assertEquals("higher level step", outerErr.displayString(alternate = false))
        assertEquals("higher level step: inner failure", outerErr.displayString(alternate = true))
        assertSame(root, outerErr.downcastRef<RootCause>())
    }
}
