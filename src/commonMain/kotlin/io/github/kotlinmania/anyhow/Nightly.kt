// port-lint: source nightly.rs
package io.github.kotlinmania.anyhow

public object Nightly {
    /**
     * Request carrier for error-provided values.
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
