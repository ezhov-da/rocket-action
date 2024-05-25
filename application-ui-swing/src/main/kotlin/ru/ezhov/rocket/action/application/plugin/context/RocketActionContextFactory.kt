package ru.ezhov.rocket.action.application.plugin.context

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.api.context.variables.VariablesService
import ru.ezhov.rocket.action.application.search.application.SearchService

@Service
class RocketActionContextFactory(
    private val icon: IconService,
    private val notification: NotificationService,
    private val cache: CacheService,
    private val variables: VariablesService,
    private val search: SearchService,
) {

    val context: RocketActionContext = object : RocketActionContext {
        override fun icon(): IconService = icon

        override fun notification(): NotificationService = notification

        override fun cache(): CacheService = cache

        override fun variables(): VariablesService = variables

        override fun search(): Search = search.search()
    }
}
