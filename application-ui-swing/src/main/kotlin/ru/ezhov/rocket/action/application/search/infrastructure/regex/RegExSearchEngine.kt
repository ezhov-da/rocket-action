package ru.ezhov.rocket.action.application.search.infrastructure.regex

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.search.domain.SearchEngine
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

private val logger = KotlinLogging.logger { }

class RegExSearchEngine : SearchEngine {
    private val map = ConcurrentHashMap<String, LinkedBlockingQueue<String>>()

    override fun description(): String = "Regular expression support"

    override fun register(id: String, text: String) {
        logger.debug { "Register action with id='$id' with text='${text.take(40)}' for search" }

        map.getOrPut(id) { LinkedBlockingQueue<String>() }.add(text)
    }

    override fun ids(text: String): List<String> {
        val regex = text.toRegex()

        val result = map.filter { it.value.any { v -> regex.containsMatchIn(v) } }

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result.keys.toList()
    }

    override fun delete(id: String) {
        logger.debug { "Delete action with id='$id' from search" }

        map.remove(id)
    }
}
