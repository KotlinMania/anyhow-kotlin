// port-lint: source src/fmt.rs
package io.github.kotlinmania.anyhow

/**
 * Kotlin translation of the upstream `ErrorImpl::display` formatter.
 */
internal fun ErrorImplDisplay(thisRef: Ref<ErrorImpl>, alternate: Boolean): String {
    val out = StringBuilder()
    val error = errorImplError(thisRef)
    out.append(error.toString())

    if (alternate) {
        val chain = errorImplChain(thisRef)
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
    val error = errorImplError(thisRef)

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

    val backtrace = errorImplBacktrace(thisRef)
    if (backtrace.status() == BacktraceStatus.Captured) {
        out.append("\n\n")
        out.append(backtrace.toString())
    }

    return out.toString()
}

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
