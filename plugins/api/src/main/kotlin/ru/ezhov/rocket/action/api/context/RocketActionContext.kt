package ru.ezhov.rocket.action.api.context

import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.api.context.variables.VariablesService

/**
 * Auxiliary context providing functionality for convenient creation of an action
 */
interface RocketActionContext {
    /**
     * Icon service
     */
    fun icon(): IconService

    /**
     * Service for working with notifications
     */
    fun notification(): NotificationService

    /**
     * Caching service
     */
    fun cache(): CacheService

    /**
     * Service providing variables
     */
    fun variables(): VariablesService = object : VariablesService {
        override fun variables(): Map<String, String> = emptyMap()
    }

    /**
     * Search registration service
     */
    fun search(): Search
}
