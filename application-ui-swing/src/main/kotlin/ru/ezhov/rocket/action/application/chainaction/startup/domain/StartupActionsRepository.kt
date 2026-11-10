package ru.ezhov.rocket.action.application.chainaction.startup.domain

import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions

interface StartupActionsRepository {
    fun all(): StartupActions

    fun save(startupActions: StartupActions)
}
