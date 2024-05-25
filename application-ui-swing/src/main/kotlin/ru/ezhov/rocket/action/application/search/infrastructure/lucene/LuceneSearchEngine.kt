package ru.ezhov.rocket.action.application.search.infrastructure.lucene

import mu.KotlinLogging
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.index.Term
import org.apache.lucene.store.ByteBuffersDirectory
import ru.ezhov.rocket.action.application.search.domain.SearchEngine

private val logger = KotlinLogging.logger { }

class LuceneSearchEngine : SearchEngine {
    private val inMemoryLuceneIndex = InMemoryLuceneIndex(ByteBuffersDirectory(), RussianAnalyzer())

    override fun description(): String = "Lucene search engine support"

    override fun register(id: String, text: String) {
        logger.debug { "Register action with id='$id' with text='${text.take(40)}' for search" }

        inMemoryLuceneIndex.indexDocument(
            mapOf(
                "id" to id,
                "text" to text,
            )
        )
    }

    override fun ids(text: String): List<String> {
        val result = inMemoryLuceneIndex
            .searchIndex("text", text).map { it.get("id") }
            .toSet()
            .toList()

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result
    }

    override fun delete(id: String) {
        logger.debug { "Delete action with id='$id' from search" }

        inMemoryLuceneIndex.deleteDocument(Term("id", id))
    }
}
