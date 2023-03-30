package ru.ezhov.rocket.action.application.search.application

object SearchInstance {
    private val searchService = SearchService()

    fun service(): SearchService = searchService
}
