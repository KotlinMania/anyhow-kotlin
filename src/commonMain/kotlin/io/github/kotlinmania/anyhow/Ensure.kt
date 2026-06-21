// port-lint: source ensure.rs
package io.github.kotlinmania.anyhow

internal interface BothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> bothDebug(pair: Pair<A, B>): BothDebug =
    object : BothDebug {
        override fun dispatchEnsure(msg: String): Error = render(msg, pair.first, pair.second)
    }

internal interface NotBothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> notBothDebug(pair: Pair<A, B>): NotBothDebug {
    return object : NotBothDebug {
        override fun dispatchEnsure(msg: String): Error {
            val ignored = pair
            return Error.msg(msg)
        }
    }
}

private class Buf {
    private val out = StringBuilder()

    fun asString(): String = out.toString()

    fun writeString(s: String) {
        if (s.any { it == ' ' || it == '\n' }) {
            throw IllegalArgumentException("unsupported")
        }

        val remaining = 40 - out.length
        if (s.length > remaining) {
            throw IllegalArgumentException("unsupported")
        }

        out.append(s)
    }
}

private fun render(
    msg: String,
    lhs: Any?,
    rhs: Any?,
): Error =
    try {
        val lhsBuf = Buf()
        lhsBuf.writeString(lhs.toString())

        val rhsBuf = Buf()
        rhsBuf.writeString(rhs.toString())

        val lhsStr = lhsBuf.asString()
        val rhsStr = rhsBuf.asString()
        Error.msg("$msg ($lhsStr vs $rhsStr)")
    } catch (_: IllegalArgumentException) {
        Error.msg(msg)
    }

internal fun <L, R> fancyEnsure(
    lhs: L,
    rhs: R,
    op: (L, R) -> Boolean,
    msg: String,
): Error? {
    val ok = op(lhs, rhs)
    if (ok) return null
    return bothDebug(Pair(lhs, rhs)).dispatchEnsure(msg)
}
