// port-lint: source src/fmt.rs
package io.github.kotlinmania.anyhow

/**
 * Kotlin translation of the upstream `ErrorImpl::display` formatter.
 */
internal fun ErrorImplDisplay(thisRef: Ref<ErrorImpl>, alternate: Boolean): String {
    val out = StringBuilder()
    val error = ErrorImplError(thisRef)
    out.append(error.toString())

    if (alternate) {
        val chain = ErrorImplChain(thisRef)
        var first = true
        for (cause in chain) {
            if (first) {
                first = false
                continue
            }
            out.append(": ")
            out.append(cause.toString())
        }
    }

    return out.toString()
}

/**
 * Kotlin translation of the upstream `ErrorImpl::debug` formatter.
 */
internal fun ErrorImplDebug(thisRef: Ref<ErrorImpl>, alternate: Boolean): String {
    val out = StringBuilder()
    val error = ErrorImplError(thisRef)

    if (alternate) {
        out.append(error.toString())
        return out.toString()
    }

    out.append(error.toString())

    val cause = error.source()
    if (cause != null) {
        out.append("\n\nCaused by:")
        val multiple = cause.source() != null
        var index = 0
        val chain = chainNew(cause)
        for (err in chain) {
            out.append('\n')
            val indented = Indented(
                inner = out,
                number = if (multiple) index else null,
                started = false,
            )
            indented.writeStr(err.toString())
            index += 1
        }
    }

    return out.toString()
}

/**
 * Placeholder call sites for the upstream `ErrorImpl::error` and `ErrorImpl::chain` functions,
 * which are translated in `Error.kt`.
 */
internal fun ErrorImplError(thisRef: Ref<ErrorImpl>): StdError = ErrorImpl.error(thisRef)

internal fun ErrorImplChain(thisRef: Ref<ErrorImpl>): Chain = ErrorImpl.chain(thisRef)

/**
 * Indenting adapter used by anyhow's debug formatting.
 *
 * This is a Kotlin translation of the upstream `Indented` writer adapter used to prefix each line
 * with either:
 *
 * - a right-aligned numeric index (`"    2: "`), or
 * - a fixed four-space indent (`"    "`),
 *
 * and to align subsequent lines under the message body.
 */
internal class Indented(
    private val inner: Appendable,
    private val number: Int?,
    private var started: Boolean,
) {
    fun writeStr(s: String) {
        val lines = s.split('\n')
        for ((i, line) in lines.withIndex()) {
            if (!started) {
                started = true
                if (number != null) {
                    inner.append(number.toString().padStart(5, ' '))
                    inner.append(": ")
                } else {
                    inner.append("    ")
                }
            } else if (i > 0) {
                inner.append('\n')
                if (number != null) {
                    inner.append("       ")
                } else {
                    inner.append("    ")
                }
            }

            inner.append(line)
        }
    }
}
