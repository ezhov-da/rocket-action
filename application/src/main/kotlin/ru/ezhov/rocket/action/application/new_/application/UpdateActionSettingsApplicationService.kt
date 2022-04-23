package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings

interface UpdateActionSettingsApplicationService {
    fun `do`(settings: ActionSettings)
}
