package ru.ezhov.rocket.action.application.core.domain.model

import java.time.LocalDateTime

data class ActionsModel(
    val lastChangedDate: LocalDateTime = LocalDateTime.now(),
    val actions: List<RocketActionSettingsModel>
)
