package ru.ezhov.rocket.action.application.chainaction.scheduler.domain

import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulers

interface ActionSchedulerRepository {
    fun all(): ActionSchedulers

    fun save(schedulers: ActionSchedulers)

    fun save(scheduler: ActionScheduler)

    fun delete(actionId: String)
}
