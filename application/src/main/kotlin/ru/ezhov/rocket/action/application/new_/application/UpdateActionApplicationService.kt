package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings

interface UpdateActionApplicationService {
    fun `do`(action: Action)
}
