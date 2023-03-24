package ru.ezhov.rocket.action.application.variables.infrastructure.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonVariablesDto(
    val encryption: JsonEncryptionDto? = null,
    val variables: List<JsonVariableDto>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonEncryptionDto(
    val algorithm: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class JsonVariableDto(
    val name: String,
    val description: String,
    val value: String,
)
