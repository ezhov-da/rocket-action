package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder

@JsonIgnoreProperties(ignoreUnknown = true)
class ChainActionDto(
    var id: String,
    var name: String,
    var description: String,
    var actions: List<ActionOrder>,
)
