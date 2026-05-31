@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source backtrace.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
public class Backtrace internal constructor(
    private val captured: String,
) {
    public fun status(): BacktraceStatus = if (captured.isEmpty()) BacktraceStatus.Unsupported else BacktraceStatus.Captured

    override fun toString(): String {
        val status = status()
        if (status == BacktraceStatus.Unsupported) return "unsupported backtrace"
        if (status == BacktraceStatus.Disabled) return "disabled backtrace"
        return captured
    }

    public companion object {
        public fun capture(): Backtrace = Backtrace(Throwable().stackTraceToString())
    }
}

@HiddenFromObjC
public enum class BacktraceStatus {
    Unsupported,
    Disabled,
    Captured,
}

internal fun backtrace(): Backtrace? = Backtrace.capture()

internal fun backtraceIfAbsent(err: Throwable): Backtrace? = if (err.stackTraceToString().isEmpty()) backtrace() else null

internal fun backtraceIfAbsent(err: StdError): Backtrace? =
    when (err) {
        is Throwable -> backtraceIfAbsent(err as Throwable)
        else -> backtrace()
    }
