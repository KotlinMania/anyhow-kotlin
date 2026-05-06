// port-lint: source src/nightly.rs
package io.github.kotlinmania.anyhow

/**
 * This file exists in the Rust upstream to probe/abstract over the `core::error::Error`
 * generic member access API (notably backtrace provisioning).
 *
 * Kotlin has no equivalent mechanism, so the Kotlin port exposes the same surface-level
 * helpers in terms of Kotlin's `Throwable` and its stacktrace string.
 */
public object Nightly {
    /**
     * Rust's `core::error::Request` carries typed requests for error-provided values.
     * In Kotlin we model this as a minimal carrier for a single optional backtrace.
     */
    public class Request {
        internal var backtrace: Backtrace? = null

        public fun provideRef(backtrace: Backtrace) {
            this.backtrace = backtrace
        }
    }

    public fun requestRefBacktrace(err: Throwable): Backtrace? {
        return Backtrace(err.stackTraceToString())
    }

    public fun provideRefBacktrace(request: Request, backtrace: Backtrace) {
        request.provideRef(backtrace)
    }

    public fun provide(err: Throwable, request: Request) {
        provideRefBacktrace(request, Backtrace(err.stackTraceToString()))
    }
}
