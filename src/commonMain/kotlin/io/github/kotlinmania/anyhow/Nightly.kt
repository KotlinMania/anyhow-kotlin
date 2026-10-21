// port-lint: source nightly.rs

package io.github.kotlinmania.anyhow

// This code exercises the surface area that we expect of the Error generic
// member access API.

internal class MyError(
    internal val backtrace: Backtrace,
) : StdError

internal fun MyError.fmt(formatter: Appendable): Appendable = formatter.append(backtrace.toString())

internal fun MyError.fmt(
    formatter: Appendable,
    debug: Boolean,
): Appendable = if (debug) formatter.append(backtrace.toString()) else formatter.append(backtrace.toString())

internal fun MyError.provide(request: Nightly.Request) {
    Nightly.provideRefBacktrace(request, backtrace)
}

internal fun requestRefBacktrace(err: StdError): Backtrace? = Nightly.requestRefBacktrace(err)

internal inline fun <reified T : Any> requestRef(err: StdError): T? {
    val request = Nightly.Request()
    Nightly.provide(err, request)
    return request.backtrace as? T
}

internal fun provideRefBacktrace(
    request: Nightly.Request,
    backtrace: Backtrace,
) {
    Nightly.provideRefBacktrace(request, backtrace)
}

internal fun provide(
    err: StdError,
    request: Nightly.Request,
) {
    Nightly.provide(err, request)
}

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

    public fun requestRefBacktrace(err: StdError): Backtrace? = requestRef<Backtrace>(err)

    public fun provideRefBacktrace(
        request: Request,
        backtrace: Backtrace,
    ) {
        request.provideRef(backtrace)
    }

    public fun provide(
        err: StdError,
        request: Request,
    ) {
        when (err) {
            is MyError -> err.provide(request)
            is BoxedError -> err.provide(request)
            is ThrowableStdError -> provideRefBacktrace(request, Backtrace(err.value.stackTraceToString()))
        }
    }
}
