// port-lint: source fmt.rs
package io.github.kotlinmania.anyhow

internal fun display(
    thisRef: Ref<ErrorImpl>,
    alternate: Boolean,
): String {
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

internal fun debug(
    thisRef: Ref<ErrorImpl>,
    alternate: Boolean,
): String {
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
            val indented =
                Indented(
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
