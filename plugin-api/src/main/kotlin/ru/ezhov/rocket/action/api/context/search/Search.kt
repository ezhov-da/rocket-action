package ru.ezhov.rocket.action.api.context.search

/**
 * Search by actions.
 *
 * Allows an action to register itself for search
 */
interface Search {
    /**
     * Register action
     *
     * @param id action ID
     * @param text the text by which the action will be searched
     */
    fun register(id: String, text: String)

    /**
     * Delete an action registered for a search
     *
     * @param id action ID
     */
    fun delete(id: String)
}
