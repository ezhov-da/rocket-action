package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface GetActionApplicationService {
    fun children(id: ActionId): List<Action>
    fun action(id: ActionId): Action?
}
