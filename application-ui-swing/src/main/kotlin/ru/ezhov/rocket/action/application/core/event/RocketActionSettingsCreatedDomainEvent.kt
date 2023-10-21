package ru.ezhov.rocket.action.application.core.event

import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

data class RocketActionSettingsCreatedDomainEvent(
    val groupId: String? = null,
    val rocketActionSettingsModel: RocketActionSettingsModel,
) : DomainEvent
