package ru.ezhov.rocket.action.application.domain

import ru.ezhov.rocket.action.application.domain.model.ActionsModel

interface RocketActionSettingsRepository {
    fun actions(): ActionsModel

    fun save(actions: ActionsModel)
}
