package ru.ezhov.rocket.action.plugin.markdown

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Image
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.AttributeProvider
import org.commonmark.renderer.html.HtmlRenderer

object Markdown {
    fun textMarkdownToHtml(text: String): String =
        markdownToHtml(text)

    fun resourceMarkdownToHtml(resource: String): String =
        markdownToHtml(
            Markdown::class.java.getResourceAsStream(resource)!!
                .use { it.bufferedReader().readText() }
        )

    private fun markdownToHtml(markdown: String): String {
        // https://github.com/commonmark/commonmark-java
        val extensions = listOf(TablesExtension.create())
        val parser: Parser = Parser.builder().extensions(extensions).build()
        val document: Node = parser.parse(markdown)

        val renderer: HtmlRenderer = HtmlRenderer.builder()
            .extensions(extensions)
            .attributeProviderFactory { ImageAttributeProvider() }
            .build()
        return renderer.render(document)
    }

    internal class ImageAttributeProvider : AttributeProvider {
        override fun setAttributes(node: Node, tagName: String, attributes: MutableMap<String, String>) {
            if (node is Image) {
                val src = attributes["src"]!!
                if (!src.startsWith("http")) {
                    attributes["src"] = ImageAttributeProvider::class.java.getResource(src)!!.toExternalForm()
                }
            }
        }
    }
}




