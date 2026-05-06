// port-lint: source src/fmt.rs
package io.github.kotlinmania.anyhow

import kotlin.test.Test
import kotlin.test.assertEquals

class IndentedTest {
    @Test
    fun oneDigit() {
        val input = "verify\nthis"
        val expected = "    2: verify\n       this"
        val output = StringBuilder()

        Indented(
            inner = output,
            number = 2,
            started = false,
        ).writeStr(input)

        assertEquals(expected, output.toString())
    }

    @Test
    fun twoDigits() {
        val input = "verify\nthis"
        val expected = "   12: verify\n       this"
        val output = StringBuilder()

        Indented(
            inner = output,
            number = 12,
            started = false,
        ).writeStr(input)

        assertEquals(expected, output.toString())
    }

    @Test
    fun noDigits() {
        val input = "verify\nthis"
        val expected = "    verify\n    this"
        val output = StringBuilder()

        Indented(
            inner = output,
            number = null,
            started = false,
        ).writeStr(input)

        assertEquals(expected, output.toString())
    }
}

