package ru.ezhov.rocket.action.application.search.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.application.search.infrastructure.regex.RegExSearchEngine

private val logger = KotlinLogging.logger { }

@Service
class SearchService {
    private val searchEngine = RegExSearchEngine()

    fun search(text: String): List<String> {
        val result = searchEngine.ids(text)

        logger.info { "Found actions by text='$text'. Count '${result.size}'" }

        return result
    }

    fun searchDescription(): String = "Regular expression support"

    fun search(): Search = object : Search {
        override fun register(id: String, text: String) {
            logger.debug { "Register action with id='$id' with text='${text.take(40)}' for search" }

            searchEngine.register(id, text)
        }

        // TODO when to delete data when deleting an action?
        override fun delete(id: String) {
            searchEngine.delete(id)
        }
    }
}
