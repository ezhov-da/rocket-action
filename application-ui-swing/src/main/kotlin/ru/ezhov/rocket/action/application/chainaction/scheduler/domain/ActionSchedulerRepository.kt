package ru.ezhov.rocket.action.application.chainaction.scheduler.domain

import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulers

interface ActionSchedulerRepository {
    fun all(): ActionSchedulers

    /**
     * Returns the scheduler for the action without loading the actions the scheduler points to.
     */
    fun byActionId(actionId: String): ActionScheduler?

    fun save(schedulers: ActionSchedulers)

    fun save(scheduler: ActionScheduler)

    fun delete(actionId: String)
}
