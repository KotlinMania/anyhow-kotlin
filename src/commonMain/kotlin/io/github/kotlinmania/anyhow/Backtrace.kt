// port-lint: source backtrace.rs

package io.github.kotlinmania.anyhow

public class Backtrace internal constructor(
    internal val inner: Inner,
) {
    internal constructor(captured: String) : this(
        if (captured.isEmpty()) {
            Inner.Unsupported
        } else {
            val frames =
                captured
                    .lines()
                    .filter { it.isNotEmpty() }
                    .map { BacktraceFrame(frame = it, symbols = mutableListOf()) }
                    .toMutableList()
            Inner.Captured(
                LazilyResolvedCapture.new(
                    Capture(
                        actualStart = 0,
                        resolved = false,
                        frames = frames,
                        rawText = captured,
                    ),
                ),
            )
        },
    )

    override fun toString(): String = fmt(StringBuilder()).toString()

    public companion object
}

internal fun Backtrace.fmt(
    fmt: Appendable,
    debug: Boolean,
): Appendable {
    if (!debug) {
        return fmt(fmt)
    }
    val capture =
        when (val current = inner) {
            is Inner.Unsupported -> return fmt.append("<unsupported>")
            is Inner.Disabled -> return fmt.append("<disabled>")
            is Inner.Captured -> current.capture.force()
        }

    val frames = capture.frames.subList(capture.actualStart.coerceAtMost(capture.frames.size), capture.frames.size)
    fmt.append("Backtrace [")
    var first = true
    for (frame in frames) {
        if (frame.frame.isEmpty()) {
            continue
        }
        for (symbol in frame.symbols) {
            if (!first) {
                fmt.append(", ")
            }
            first = false
            symbol.fmt(fmt)
        }
    }
    fmt.append("]")
    return fmt
}

internal fun Backtrace.Companion.enabled(): Boolean = true

public fun Backtrace.Companion.capture(): Backtrace =
    if (enabled()) {
        create(0)
    } else {
        Backtrace(Inner.Disabled)
    }

internal fun Backtrace.Companion.create(ip: Int): Backtrace {
    val captured = Throwable().stackTraceToString()
    val lines = captured.lines().filter { it.isNotEmpty() }
    if (lines.isEmpty()) {
        return Backtrace(Inner.Unsupported)
    }
    val frames = mutableListOf<BacktraceFrame>()
    var actualStart: Int? = null
    for ((index, line) in lines.withIndex()) {
        frames.add(BacktraceFrame(frame = line, symbols = mutableListOf()))
        if (index == ip && actualStart == null && ip > 0) {
            actualStart = frames.size
        }
    }
    val inner =
        Inner.Captured(
            LazilyResolvedCapture.new(
                Capture(
                    actualStart = actualStart ?: 0,
                    resolved = false,
                    frames = frames,
                    rawText = captured,
                ),
            ),
        )
    return Backtrace(inner)
}

public fun Backtrace.status(): BacktraceStatus =
    when (inner) {
        is Inner.Unsupported -> BacktraceStatus.Unsupported
        is Inner.Disabled -> BacktraceStatus.Disabled
        is Inner.Captured -> BacktraceStatus.Captured
    }

internal fun Backtrace.fmt(fmt: Appendable): Appendable {
    val capture =
        when (val current = inner) {
            is Inner.Unsupported -> return fmt.append("unsupported backtrace")
            is Inner.Disabled -> return fmt.append("disabled backtrace")
            is Inner.Captured -> current.capture.force()
        }

    if (capture.rawText.isNotEmpty()) {
        return fmt.append(capture.rawText)
    }

    val frames = capture.frames.subList(capture.actualStart.coerceAtMost(capture.frames.size), capture.frames.size)
    var first = true
    for (frame in frames) {
        if (!first) {
            fmt.append('\n')
        }
        first = false
        fmt.append(frame.frame)
    }
    return fmt
}

public enum class BacktraceStatus {
    Unsupported,
    Disabled,
    Captured,
}

internal sealed class Inner {
    internal data object Unsupported : Inner()

    internal data object Disabled : Inner()

    internal data class Captured(
        val capture: LazilyResolvedCapture,
    ) : Inner()
}

internal class Capture(
    internal val actualStart: Int,
    internal var resolved: Boolean,
    internal val frames: MutableList<BacktraceFrame>,
    internal val rawText: String = "",
)

internal fun Capture.resolve() {
    if (resolved) {
        return
    }
    resolved = true

    for (frame in frames) {
        val symbols = frame.symbols
        val raw = frame.frame.trim()
        if (raw.isNotEmpty() && symbols.isEmpty()) {
            symbols.add(
                BacktraceSymbol(
                    name = raw.encodeToByteArray(),
                    filename = null,
                    lineno = null,
                    colno = null,
                ),
            )
        }
    }
}

internal class BacktraceFrame(
    internal val frame: String,
    internal val symbols: MutableList<BacktraceSymbol>,
)

internal fun BacktraceFrame.fmt(fmt: Appendable): Appendable {
    fmt.append('[')
    for ((index, symbol) in symbols.withIndex()) {
        if (index > 0) {
            fmt.append(", ")
        }
        symbol.fmt(fmt)
    }
    fmt.append(']')
    return fmt
}

internal class BacktraceSymbol(
    internal val name: ByteArray?,
    internal val filename: BytesOrWide?,
    internal val lineno: UInt?,
    internal val colno: UInt?,
)

internal fun BacktraceSymbol.fmt(fmt: Appendable): Appendable {
    fmt.append("{ ")
    if (name != null) {
        fmt.append("fn: \"")
        fmt.append(name.decodeToString())
        fmt.append('"')
    } else {
        fmt.append("fn: <unknown>")
    }

    if (filename != null) {
        fmt.append(", file: \"")
        filename.fmt(fmt)
        fmt.append('"')
    }

    if (lineno != null) {
        fmt.append(", line: ")
        fmt.append(lineno.toString())
    }

    fmt.append(" }")
    return fmt
}

internal sealed class BytesOrWide {
    internal class Bytes(
        internal val value: ByteArray,
    ) : BytesOrWide()

    internal class Wide(
        internal val value: ShortArray,
    ) : BytesOrWide()
}

internal fun BytesOrWide.fmt(fmt: Appendable): Appendable = outputFilename(fmt, this, shortFormat = true, cwd = null)

internal class LazilyResolvedCapture internal constructor(
    internal val capture: Capture,
) {
    internal companion object
}

internal fun LazilyResolvedCapture.Companion.new(capture: Capture): LazilyResolvedCapture = LazilyResolvedCapture(capture)

internal fun LazilyResolvedCapture.force(): Capture {
    capture.resolve()
    return capture
}

internal fun outputFilename(
    fmt: Appendable,
    bows: BytesOrWide,
    shortFormat: Boolean,
    cwd: String?,
): Appendable {
    val file =
        when (bows) {
            is BytesOrWide.Bytes -> bows.value.decodeToString()
            is BytesOrWide.Wide -> CharArray(bows.value.size) { bows.value[it].toInt().toChar() }.concatToString()
        }
    if (shortFormat && cwd != null && file.startsWith(cwd)) {
        val stripped = file.removePrefix(cwd).trimStart('/', '\\')
        if (stripped.isNotEmpty()) {
            fmt.append("./")
            fmt.append(stripped)
            return fmt
        }
    }
    fmt.append(file)
    return fmt
}

internal fun assertSendSync() {
    assert<Backtrace>()
}

internal fun <T> assert() = Unit

internal fun backtrace(): Backtrace? = Backtrace.capture()

internal fun backtraceIfAbsent(err: Throwable): Backtrace? = if (err.stackTraceToString().isEmpty()) backtrace() else null

internal fun backtraceIfAbsent(err: StdError): Backtrace? =
    when (err) {
        is Throwable -> backtraceIfAbsent(err as Throwable)
        else -> backtrace()
    }
