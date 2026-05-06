// port-lint: source src/ensure.rs
package io.github.kotlinmania.anyhow

/**
 * Kotlin translation of the upstream `ensure.rs`.
 *
 * The Rust upstream file is largely macro machinery that powers `ensure!(...)` and its fancy
 * comparison rendering (`lhs vs rhs`) for certain syntactic forms.
 *
 * Kotlin has no macro system, so this port cannot replicate token parsing (`macro_rules!`) or the
 * ability to return early from the caller’s function body. The Kotlin port instead provides:
 *
 * - explicit helpers in `Macros.kt` (`ensure(cond, ...)`) for the public behavior, and
 * - the same internal message rendering logic for the "fancy ensure" case, implemented as normal
 *   functions.
 */

/**
 * Internal hook used by the upstream macro to choose whether it can render `lhs` and `rhs` inline.
 *
 * In Rust, the specialization is based on whether both sides implement `Debug`. Kotlin’s closest
 * analog is `toString()`. This port preserves the structure and behavior:
 *
 * - If the debug strings are short (≤ 40) and contain no spaces or newlines, we render
 *   `{msg} ({lhs} vs {rhs})`.
 * - Otherwise we fall back to `Error.msg(msg)`.
 */
internal interface BothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> bothDebug(pair: Pair<A, B>): BothDebug {
    return object : BothDebug {
        override fun dispatchEnsure(msg: String): Error {
            return render(msg, pair.first, pair.second)
        }
    }
}

/**
 * Internal hook representing the upstream "lower precedence" branch where we skip fancy rendering.
 *
 * In Rust, this is implemented for `&(A, B)` so it loses method resolution against the real
 * `BothDebug` impl. Kotlin has no such dispatch, but the type is kept to match the upstream shape.
 */
internal interface NotBothDebug {
    fun dispatchEnsure(msg: String): Error
}

internal fun <A, B> notBothDebug(pair: Pair<A, B>): NotBothDebug {
    return object : NotBothDebug {
        override fun dispatchEnsure(msg: String): Error {
            val _ignored = pair
            return Error.msg(msg)
        }
    }
}

/**
 * Fixed-size buffer used to attempt rendering `lhs` and `rhs` without allocations.
 *
 * The Rust upstream uses a `[MaybeUninit<u8>; 40]` buffer and rejects strings containing spaces or
 * newlines. This port uses a fixed-capacity [StringBuilder] and enforces the same constraints.
 */
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

private fun render(msg: String, lhs: Any?, rhs: Any?): Error {
    return try {
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
}

/**
 * Kotlin equivalent of the upstream `__fancy_ensure!` behavior.
 *
 * This helper evaluates both sides once and, if the comparison fails, constructs an [Error] whose
 * message includes the rendered `lhs` and `rhs` when possible.
 */
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
