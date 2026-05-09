// port-lint: source wrapper.rs
package io.github.kotlinmania.anyhow

internal class MessageError<M>(
    internal val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

internal class DisplayError<M>(
    internal val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

internal class BoxedError(
    internal val value: StdError,
) : StdError {
    override fun source(): StdError? = value.source()

    override fun toString(): String = value.toString()
}
