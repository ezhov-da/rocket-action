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
        val searchText = text.lowercase().trim()

        val result = map.filter { it.value.any { v -> v.lowercase().contains(searchText) } }

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result.keys.toList()
    }

    override fun delete(id: String) {
        logger.debug { "Delete action with id='$id' from search" }

        map.remove(id)
    }
}
