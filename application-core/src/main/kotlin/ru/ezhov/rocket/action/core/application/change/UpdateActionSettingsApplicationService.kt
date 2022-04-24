package ru.ezhov.rocket.action.core.application.change

import ru.ezhov.rocket.action.core.domain.model.ActionSettings

interface UpdateActionSettingsApplicationService {
    fun `do`(settings: ActionSettings)
}
