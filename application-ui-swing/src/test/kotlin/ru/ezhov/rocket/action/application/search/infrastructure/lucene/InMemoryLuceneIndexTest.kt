package ru.ezhov.rocket.action.application.search.infrastructure.lucene

import org.apache.lucene.analysis.ru.RussianAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.store.ByteBuffersDirectory
import org.junit.jupiter.api.Test

internal class InMemoryLuceneIndexTest {
    @Test
    fun `should found`() {
        val inMemoryLuceneIndex = InMemoryLuceneIndex(ByteBuffersDirectory(), RussianAnalyzer())
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "Some hello world"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "Отсутствует поиск hello world"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "hello"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "http://rest/api стандарт word"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "http://rest/api стандарт бла"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "http://rest/api стандарт б"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "http://rest/api станда"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "Log analysis rules"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "Регламент анализа проведения логов"))
        inMemoryLuceneIndex.indexDocument(mapOf("text" to "Регламент проведения анализа логов"))

        var documents: List<Document>

        documents = inMemoryLuceneIndex.searchIndex("text", "so world")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "so")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "станда бла")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "станда")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "анализ логов")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "api")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "api станд")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "анал лог")
        println(documents)

        documents = inMemoryLuceneIndex.searchIndex("text", "регламен")
        println(documents)
    }
}
