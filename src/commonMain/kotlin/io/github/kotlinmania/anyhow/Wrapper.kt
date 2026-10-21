// port-lint: source wrapper.rs
package io.github.kotlinmania.anyhow

internal class MessageError<M>(
    internal val value: M,
) : StdError {
    override fun toString(): String = fmt(StringBuilder()).toString()
}

internal fun <M> MessageError<M>.fmt(f: Appendable): Appendable = f.append(value.toString())

internal fun <M> MessageError<M>.fmt(
    f: Appendable,
    debug: Boolean,
): Appendable = if (debug) f.append(value.toString()) else f.append(value.toString())

internal class DisplayError<M>(
    internal val value: M,
) : StdError {
    override fun toString(): String = fmt(StringBuilder()).toString()
}

internal fun <M> DisplayError<M>.fmt(f: Appendable): Appendable = f.append(value.toString())

internal fun <M> DisplayError<M>.fmt(
    f: Appendable,
    debug: Boolean,
): Appendable = if (debug) f.append(value.toString()) else f.append(value.toString())

internal class BoxedError(
    internal val value: StdError,
) : StdError {
    override fun source(): StdError? = source(this)

    override fun toString(): String = fmt(StringBuilder()).toString()

    override fun debugString(alternate: Boolean): String = value.debugString(alternate)
}

internal fun BoxedError.fmt(f: Appendable): Appendable = f.append(value.toString())

internal fun BoxedError.fmt(
    f: Appendable,
    debug: Boolean,
): Appendable = if (debug) f.append(value.toString()) else f.append(value.toString())

internal fun source(boxedError: BoxedError): StdError? = boxedError.value.source()

internal fun BoxedError.provide(request: Nightly.Request) {
    Nightly.provide(value, request)
}
