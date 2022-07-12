package ru.ezhov.rocket.action.plugin.noteonfile

import org.junit.Test

internal class CalculatePointServiceTest {
    @Test
    fun `valid test`() {
        val text = """
        111111111111111
        >>> 22222222222
        aaaaaaaaaaaaa
        bbbbb
        >>> 33333333333
        >>>
        4444444444
        >>> 55555555555

        """.trimIndent()

        println(CalculatePointService().calculate(">>>", text))
    }
}
