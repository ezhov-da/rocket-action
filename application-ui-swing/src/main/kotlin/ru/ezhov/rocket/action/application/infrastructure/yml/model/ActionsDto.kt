package ru.ezhov.rocket.action.application.infrastructure.yml.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class ActionsDto(
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastChangedDate: LocalDateTime = LocalDateTime.now(),
    val actions: List<RocketActionSettingsDto>,
)
