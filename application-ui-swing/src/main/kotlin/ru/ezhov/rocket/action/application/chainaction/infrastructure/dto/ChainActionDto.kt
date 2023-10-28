package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class ChainActionDto(
    var id: String,
    var name: String,
    var description: String,
    var actionIds: List<String>
)
