package ru.ezhov.rocket.action.application.core.domain

import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel

interface RocketActionSettingsRepository {
    // TODO ezhov will be processed as needed to load tags
    fun load()

    fun actions(): ActionsModel

    fun save(actions: ActionsModel)
}
