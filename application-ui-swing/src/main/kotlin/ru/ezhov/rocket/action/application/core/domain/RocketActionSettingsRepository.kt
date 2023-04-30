package ru.ezhov.rocket.action.application.core.domain

import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel

interface RocketActionSettingsRepository {
    // TODO ezhov будет перерабатываться, так как нужно для загрузки тегов
    fun load()

    fun actions(): ActionsModel

    fun save(actions: ActionsModel)
}
