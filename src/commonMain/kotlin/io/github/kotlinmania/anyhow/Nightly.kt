// port-lint: source src/nightly.rs
package io.github.kotlinmania.anyhow

/**
 * This file exists in the Rust upstream to probe/abstract over the `core::error::Error`
 * generic member access API (notably backtrace provisioning).
 *
 * Kotlin has no equivalent mechanism, so the Kotlin port exposes the same surface-level
 * helpers in terms of Kotlin's `Throwable` and its `stackTrace`.
 */
public object Nightly {
    /**
     * Rust's `core::error::Request` carries typed requests for error-provided values.
     * In Kotlin we model this as a minimal carrier for a single optional backtrace.
     */
    public class Request {
        internal var backtrace: Array<StackTraceElement>? = null

        public fun provideRef(backtrace: Array<StackTraceElement>) {
            this.backtrace = backtrace
        }
    }

    public fun requestRefBacktrace(err: Throwable): Array<StackTraceElement>? {
        return err.stackTrace
    }

    public fun provideRefBacktrace(request: Request, backtrace: Array<StackTraceElement>) {
        request.provideRef(backtrace)
    }

    public fun provide(err: Throwable, request: Request) {
        provideRefBacktrace(request, err.stackTrace)
    }
}
