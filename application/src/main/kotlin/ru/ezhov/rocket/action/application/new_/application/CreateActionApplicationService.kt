package ru.ezhov.rocket.action.application.new_.application

import ru.ezhov.rocket.action.application.new_.domain.model.NewAction

interface CreateActionApplicationService {
    fun `do`(new: NewAction) {}
}
