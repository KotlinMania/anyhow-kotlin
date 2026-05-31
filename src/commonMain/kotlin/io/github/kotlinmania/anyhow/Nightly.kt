@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

// port-lint: source nightly.rs

package io.github.kotlinmania.anyhow

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
public object Nightly {
    /**
     * Request carrier for error-provided values.
     */
    @HiddenFromObjC
    public class Request {
        internal var backtrace: Backtrace? = null

        @HiddenFromObjC
        public fun provideRef(backtrace: Backtrace) {
            this.backtrace = backtrace
        }
    }

    @HiddenFromObjC
    public fun requestRefBacktrace(err: Throwable): Backtrace? = Backtrace(err.stackTraceToString())

    @HiddenFromObjC
    public fun provideRefBacktrace(
        request: Request,
        backtrace: Backtrace,
    ) {
        request.provideRef(backtrace)
    }

    @HiddenFromObjC
    public fun provide(
        err: Throwable,
        request: Request,
    ) {
        provideRefBacktrace(request, Backtrace(err.stackTraceToString()))
    }
}
