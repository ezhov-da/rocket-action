package ru.ezhov.rocket.action.application.handlers.apikey.domain.model

data class ApiKeys(
    val keys: List<ApiKey>
) {

    companion object {
        val EMPTY = ApiKeys(keys = emptyList())
    }
}
