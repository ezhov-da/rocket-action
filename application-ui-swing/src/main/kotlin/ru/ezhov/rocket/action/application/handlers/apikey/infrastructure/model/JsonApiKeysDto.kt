package ru.ezhov.rocket.action.application.handlers.apikey.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonApiKeysDto(
    val keys: List<JsonApiKeyDto>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonApiKeyDto(
    val value: String,
    val description: String,
)
