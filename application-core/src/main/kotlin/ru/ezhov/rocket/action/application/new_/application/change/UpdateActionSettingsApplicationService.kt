package ru.ezhov.rocket.action.application.new_.application.change

import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings

interface UpdateActionSettingsApplicationService {
    fun `do`(settings: ActionSettings)
}
