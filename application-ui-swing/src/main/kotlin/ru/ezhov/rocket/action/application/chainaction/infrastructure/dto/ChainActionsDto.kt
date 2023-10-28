package ru.ezhov.rocket.action.application.chainaction.infrastructure.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ChainActionsDto(
    val changedDate: LocalDateTime,
    val chainActions: List<ChainActionDto>
)
