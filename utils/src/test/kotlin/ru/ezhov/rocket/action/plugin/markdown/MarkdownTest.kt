package ru.ezhov.rocket.action.plugin.markdown

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MarkdownTest {
    @Test
    fun `two conversions produce identical output`() {
        val text = "**Name**. _alias_"

        assertThat(Markdown.textMarkdownToHtml(text))
            .isEqualTo(Markdown.textMarkdownToHtml(text))
    }

    @Test
    fun `the parser and renderer are reused across conversions`() {
        Markdown.textMarkdownToHtml("first")
        val parser = Markdown.parser
        val renderer = Markdown.renderer

        Markdown.textMarkdownToHtml("second")

        assertThat(Markdown.parser).isSameAs(parser)
        assertThat(Markdown.renderer).isSameAs(renderer)
    }

    @Test
    fun `renders tables from the gfm extension`() {
        val html = Markdown.textMarkdownToHtml(
            """
            | first | second |
            |-------|--------|
            | 1     | 2      |
            """.trimIndent()
        )

        assertThat(html).contains("<table>")
    }
}
