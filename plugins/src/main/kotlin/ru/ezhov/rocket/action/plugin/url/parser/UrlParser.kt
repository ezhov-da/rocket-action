package ru.ezhov.rocket.action.plugin.url.parser

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class UrlParser(
    private val url: String,
    private val headers: Map<String, String>
) {
    fun parse(filter: UrlParserFilter): UrlParserResult {
        val doc: Document =
            Jsoup
                .connect(url)
                .headers(headers)
                .get()


        val title = if (filter.readTitle) doc.title() else null
        val elementsMeta = doc.getElementsByTag("meta")
        val description = if (filter.readDescription) {
            elementsMeta
                .firstOrNull { e -> e.attr("name") == "description" }
                ?.attr("content")
        } else null

        return UrlParserResult(title = title, description = description)
    }
}
