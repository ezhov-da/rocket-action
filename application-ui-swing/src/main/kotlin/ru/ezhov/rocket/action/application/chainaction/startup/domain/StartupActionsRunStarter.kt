package ru.ezhov.rocket.action.application.chainaction.startup.domain

import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger

/**
 * Starts the startup list. Used by the startup hook and by the "Run now" button,
 * so the UI does not depend on the runner implementation.
 */
interface StartupActionsRunStarter {
    fun startAsync(trigger: StartupRunTrigger)
}
