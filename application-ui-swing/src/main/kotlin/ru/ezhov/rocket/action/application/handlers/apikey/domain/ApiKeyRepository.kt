package ru.ezhov.rocket.action.application.handlers.apikey.domain

import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKeys


interface ApiKeyRepository {
    fun all(): ApiKeys

    fun save(apiKeys: ApiKeys)
}
