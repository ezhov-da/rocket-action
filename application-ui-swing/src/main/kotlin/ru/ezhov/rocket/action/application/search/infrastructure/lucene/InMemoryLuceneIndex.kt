package ru.ezhov.rocket.action.application.search.infrastructure.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.Term
import org.apache.lucene.queryparser.classic.ParseException
import org.apache.lucene.search.FuzzyQuery
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper
import org.apache.lucene.search.spans.SpanNearQuery
import org.apache.lucene.store.Directory
import java.io.IOException


class InMemoryLuceneIndex(private val memoryIndex: Directory, private val analyzer: Analyzer) {
    fun indexDocument(values: Map<String, String>) {
        val indexWriterConfig = IndexWriterConfig(analyzer)
        try {
            val writer = IndexWriter(memoryIndex, indexWriterConfig)
            val document = Document()
            values.forEach {
                document.add(TextField(it.key, it.value.lowercase(), Field.Store.YES))
            }
            writer.addDocument(document)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun searchIndex(inField: String, queryString: String): List<Document> {
        try {
            // https://stackoverflow.com/questions/18100233/lucene-fuzzy-search-on-a-phrase-fuzzyquery-spanquery
            val query = queryString
                .lowercase()
                .split(" ")
                .let { array ->
                    when (array.size > 1) {
                        true -> {
                            val clauses = array.map { value ->
                                SpanMultiTermQueryWrapper(FuzzyQuery(Term(inField, value), ))
                            }.toTypedArray()
                            SpanNearQuery(clauses, 8, true)
                        }

                        false -> {
                            FuzzyQuery(Term(inField, array[0]))
                        }
                    }
                }

            val indexReader: IndexReader = DirectoryReader.open(memoryIndex)
            val searcher = IndexSearcher(indexReader)
            val topDocs = searcher.search(query, 500)
            val documents: MutableList<Document> = ArrayList()
            for (scoreDoc in topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc))
            }
            return documents
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return emptyList()
    }

    fun deleteDocument(term: Term?) {
        try {
            val indexWriterConfig = IndexWriterConfig(analyzer)
            val writer = IndexWriter(memoryIndex, indexWriterConfig)
            writer.deleteDocuments(term)
            writer.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
