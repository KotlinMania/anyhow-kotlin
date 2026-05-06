// port-lint: source src/fmt.rs
package io.github.kotlinmania.anyhow

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

