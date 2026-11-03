package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

class ContextQuoteParityTest {
    private val asciiEscapes =
        listOf(
            "\\0",
            "\\u{1}",
            "\\u{2}",
            "\\u{3}",
            "\\u{4}",
            "\\u{5}",
            "\\u{6}",
            "\\u{7}",
            "\\u{8}",
            "\\t",
            "\\n",
            "\\u{b}",
            "\\u{c}",
            "\\r",
            "\\u{e}",
            "\\u{f}",
            "\\u{10}",
            "\\u{11}",
            "\\u{12}",
            "\\u{13}",
            "\\u{14}",
            "\\u{15}",
            "\\u{16}",
            "\\u{17}",
            "\\u{18}",
            "\\u{19}",
            "\\u{1a}",
            "\\u{1b}",
            "\\u{1c}",
            "\\u{1d}",
            "\\u{1e}",
            "\\u{1f}",
            " ",
            "!",
            "\\\"",
            "#",
            "\$",
            "%",
            "&",
            "\\'",
            "(",
            ")",
            "*",
            "+",
            ",",
            "-",
            ".",
            "/",
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            ":",
            ";",
            "<",
            "=",
            ">",
            "?",
            "@",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z",
            "[",
            "\\\\",
            "]",
            "^",
            "_",
            "`",
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "v",
            "w",
            "x",
            "y",
            "z",
            "{",
            "|",
            "}",
            "~",
            "\\u{7f}",
        )

    @Test
    fun everyAsciiStringEscapeMatchesTheSource() {
        for (code in 0..127) {
            assertEquals(asciiEscapes[code], code.toChar().toString().escapeDebug(), "code $code")
        }
    }

    @Test
    fun everyAsciiCharacterEscapeMatchesTheSource() {
        for (code in 0..127) {
            assertEquals(asciiEscapes[code], code.toChar().escapeDebug(), "code $code")
        }
    }

    private class Leaf : StdError {
        override fun toString(): String = "leaf"

        override fun debugString(alternate: Boolean): String = "Leaf"
    }

    @Test
    fun publicContextDebugMatchesActualUpstreamFixtures() {
        assertEquals(
            "Error {\n    context: \"O\\'Reilly\",\n    source: Leaf,\n}",
            Error.new(Leaf()).context("O'Reilly").debugString(alternate = true),
        )
        assertEquals(
            "Error {\n    context: \"\\0\",\n    source: Leaf,\n}",
            Error.new(Leaf()).context("\u0000").debugString(alternate = true),
        )
        assertEquals(
            "Error {\n    context: \"\\\"\\\\\\n\\t\\r\",\n    source: Leaf,\n}",
            Error.new(Leaf()).context("\"\\\n\t\r").debugString(alternate = true),
        )
        assertEquals(
            "Error {\n    context: \"a\\'b\\0c\",\n    source: Leaf,\n}",
            Error.new(Leaf()).context("a'b\u0000c").debugString(alternate = true),
        )
    }
}
