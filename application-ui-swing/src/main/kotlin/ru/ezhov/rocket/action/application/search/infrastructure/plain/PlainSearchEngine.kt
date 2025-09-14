package ru.ezhov.rocket.action.application.search.infrastructure.plain

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.search.domain.SearchEngine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

private val logger = KotlinLogging.logger { }

class PlainSearchEngine : SearchEngine {
    private val map = ConcurrentHashMap<String, LinkedBlockingQueue<String>>()

    override fun description(): String = "Plain text support"

    override fun register(id: String, text: String) {
        logger.debug { "Register action with id='$id' with text='${text.take(40)}' for search" }

        map.getOrPut(id) { LinkedBlockingQueue<String>() }.add(text)
    }

    override fun ids(text: String): List<String> {
        val searchTextArrayBySpace = text.lowercase().trim().split(" ")

        val result = when (searchTextArrayBySpace.size) {
            1 -> map.filter { it.value.any { v -> v.lowercase().contains(searchTextArrayBySpace.first()) } }.keys.toList()
            else -> {
                val idsList = searchTextArrayBySpace.map { sw ->
                    map.filter { it.value.any { v -> v.lowercase().contains(sw) } }.keys.toList()
                }.flatten()

                // We group and take only those that coincided in all input words
                idsList
                    .groupingBy { it }
                    .eachCount()
                    .filter { (_, v) -> v == searchTextArrayBySpace.size }
                    .keys
            }

        }

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result.toList()
    }

    override fun delete(id: String) {
        logger.debug { "Delete action with id='$id' from search" }

        map.remove(id)
    }
}
