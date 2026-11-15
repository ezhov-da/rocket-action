package ru.ezhov.rocket.action.application

import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import ru.ezhov.rocket.action.application.event.domain.DomainEvent

/**
 * Published after the new menu content is visible on the EDT.
 *
 * [scope] carries the reload metrics so subscribers that work asynchronously (the MCP tool
 * refresh) can report their duration back into the single reload line.
 */
class MenuContentReplacedDomainEvent(
    val scope: ActionReloadScope? = null,
) : DomainEvent
