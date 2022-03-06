package ru.ezhov.rocket.action.url.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class UrlParser(
    private val url: String,
    private val headers: Map<String, String>
) {
    fun parse(): String {
        val doc: Document =
            Jsoup
                .connect(url)
                .headers(headers)
                .get()
        val title = doc.title()
        val elementsMeta = doc.getElementsByTag("meta")
        val description = elementsMeta
            .firstOrNull { e -> e.attr("name") == "description" }
            ?.attr("content")

        return """
                    ${title.orEmpty()}
                    ${description.orEmpty()}
                """.trimIndent()
    }
}