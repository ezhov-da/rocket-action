package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface ChangeOrderActionApplicationService {
    fun before(set: ActionId, before: ActionId)
    fun after(set: ActionId, after: ActionId)
}
