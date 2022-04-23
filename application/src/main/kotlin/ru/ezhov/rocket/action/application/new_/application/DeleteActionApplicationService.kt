package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.ActionId

interface DeleteActionApplicationService {
    fun `do`(id: ActionId, withAllChildren: Boolean)
}
