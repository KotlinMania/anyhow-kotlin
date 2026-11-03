// port-lint: source ensure.rs
package io.github.kotlinmania.anyhow

internal interface BothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> Pair<A, B>.dispatchEnsure(msg: String): Error = render(msg, first, second)

internal fun <A, B> bothDebug(pair: Pair<A, B>): BothDebug =
    object : BothDebug {
        override fun dispatchEnsure(msg: String): Error = pair.dispatchEnsure(msg)
    }

internal interface NotBothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> Pair<A, B>.dispatchEnsure(
    msg: String,
    fallback: Boolean,
): Error = if (fallback) Error.msg(msg) else Error.msg(msg)

internal fun <A, B> notBothDebug(pair: Pair<A, B>): NotBothDebug =
    object : NotBothDebug {
        override fun dispatchEnsure(msg: String): Error = pair.dispatchEnsure(msg, fallback = true)
    }

private class Buf(
    val bytes: ByteArray = ByteArray(40),
    var written: Int = 0,
) {
    companion object
}

private fun Buf.Companion.new(): Buf =
    Buf(
        bytes = ByteArray(40),
        written = 0,
    )

private fun Buf.asStr(): String = bytes.decodeToString(0, written)

private fun Buf.writeStr(s: String): Boolean {
    val encoded = s.encodeToByteArray()
    if (encoded.any { it == ' '.code.toByte() || it == '\n'.code.toByte() }) {
        return false
    }

    val remaining = bytes.size - written
    if (encoded.size > remaining) {
        return false
    }

    encoded.copyInto(bytes, destinationOffset = written, startIndex = 0, endIndex = encoded.size)
    written += encoded.size
    return true
}

private fun render(
    msg: String,
    lhs: Any?,
    rhs: Any?,
): Error {
    val lhsBuf = Buf.new()
    if (lhsBuf.writeStr(lhs.toString())) {
        val rhsBuf = Buf.new()
        if (rhsBuf.writeStr(rhs.toString())) {
            val lhsStr = lhsBuf.asStr()
            val rhsStr = rhsBuf.asStr()
            val len = msg.length + 2 + lhsStr.length + 4 + rhsStr.length + 1
            val string = StringBuilder(len)
            string.append(msg)
            string.append(" (")
            string.append(lhsStr)
            string.append(" vs ")
            string.append(rhsStr)
            string.append(')')
            return Error.msg(string.toString())
        }
    }
    return Error.msg(msg)
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
