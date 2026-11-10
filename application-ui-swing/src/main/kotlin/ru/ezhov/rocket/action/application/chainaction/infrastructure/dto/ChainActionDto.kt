package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder

@JsonIgnoreProperties(ignoreUnknown = true)
class ChainActionDto(
    var id: String,
    var name: String,
    var description: String,
    var actions: List<ActionOrder>,
    var icon: String?,
    /**
     * Missing in files written by older versions, so it must have a default value.
     */
    @get:JsonProperty("isTool")
    @param:JsonProperty("isTool")
    var isTool: Boolean = false,
)
