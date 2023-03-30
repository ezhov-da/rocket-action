package ru.ezhov.rocket.action.application.search.application

import mu.KotlinLogging
import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.index.Term
import org.apache.lucene.store.ByteBuffersDirectory
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.application.search.infrastructure.lucene.InMemoryLuceneIndex

private val logger = KotlinLogging.logger { }

class SearchService {
    private val inMemoryLuceneIndex = InMemoryLuceneIndex(ByteBuffersDirectory(), RussianAnalyzer())

    fun search(text: String): List<String> {
        val result =
            inMemoryLuceneIndex.searchIndex("text", text).map { it.get("id") }.toSet().toList()

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result
    }


    fun search(): Search = object : Search {
        override fun register(id: String, text: String) {
            logger.debug { "Register action with id='$id' with text='${text.take(40)}' for search" }

            inMemoryLuceneIndex.indexDocument(
                mapOf(
                    "id" to id,
                    "text" to text,
                )
            )
        }

        // TODO когда удалять данные при удалении действия?
        override fun delete(id: String) {
            logger.debug { "Delete action with id='$id' from search" }

            inMemoryLuceneIndex.deleteDocument(Term("id", id))
        }
    }
}
