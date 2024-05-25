package ru.ezhov.rocket.action.application.search.domain

interface SearchEngine {
    /**
     * Search instructions
     */
    fun description(): String

    /**
     * Register action for search
     *
     * @param id action ID
     * @param text the text by which the action will be searched
     */
    fun register(id: String, text: String)

    /**
     * Get action ids by text
     *
     * @param text the text by which the action will be searched
     */
    fun ids(text: String): List<String>

    /**
     * Delete an action registered for a search
     */
    fun delete(id: String)
}
