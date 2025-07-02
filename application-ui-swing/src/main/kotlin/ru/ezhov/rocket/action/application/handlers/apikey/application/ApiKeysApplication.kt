package ru.ezhov.rocket.action.application.handlers.apikey.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.handlers.apikey.domain.ApiKeyRepository
import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKey
import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKeys

private val logger = KotlinLogging.logger { }

@Service
class ApiKeysApplication(
    private val apiKeyRepository: ApiKeyRepository,
) {
    fun all(): ApiKeysDto =
        apiKeyRepository.all().toApiKeysDto()

    private fun encode(keys: ApiKeysDto): ApiKeys =
        ApiKeys(
            keys = keys.keys.map { v ->
                ApiKey(
                    value = v.value,
                    description = v.description,
                )
            }
        )

    fun save(keys: ApiKeysDto) {
        val result = encode(keys = keys)
        apiKeyRepository.save(result)
    }

    fun apiKey(key: String): ApiKey? =
        apiKeyRepository.all().keys.firstOrNull { it.value == key }
}

private fun ApiKeys.toApiKeysDto(): ApiKeysDto =
    ApiKeysDto(
        keys = this.keys.map {
            ApiKeyDto(
                value = it.value,
                description = it.description,
            )
        }
    )
