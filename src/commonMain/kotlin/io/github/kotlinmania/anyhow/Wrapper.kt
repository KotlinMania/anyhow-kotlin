// port-lint: source wrapper.rs
package io.github.kotlinmania.anyhow

public class MessageError<M>(
    public val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

public class DisplayError<M>(
    public val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

public class BoxedError(
    public val value: StdError,
) : StdError {
    override fun source(): StdError? = value.source()

    override fun toString(): String = value.toString()
}
