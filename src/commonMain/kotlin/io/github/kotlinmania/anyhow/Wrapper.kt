// port-lint: source src/wrapper.rs
package io.github.kotlinmania.anyhow

/**
 * Transparent wrapper error around a message value.
 *
 * In the upstream, `MessageError<M>` implements both Debug and Display by forwarding to the inner
 * message, and participates in Anyhow's downcasting behavior through its repr(transparent) layout.
 * Kotlin does not have separate Debug vs Display traits; this port forwards to [Any.toString].
 */
public class MessageError<M>(
    public val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

/**
 * Transparent wrapper error around a display-only message.
 *
 * In the upstream, `DisplayError<M>` uses Display for both Debug and Display formatting. Kotlin
 * represents both through [Any.toString].
 */
public class DisplayError<M>(
    public val value: M,
) : StdError {
    override fun toString(): String = value.toString()
}

/**
 * Boxed error wrapper.
 *
 * Upstream, this is `Box<dyn StdError + Send + Sync>` and its `source()` forwards through the
 * boxed error's source chain. This port retains that behavior in terms of [StdError].
 */
public class BoxedError(
    public val value: StdError,
) : StdError {
    override fun source(): StdError? = value.source()

    override fun toString(): String = value.toString()
}

