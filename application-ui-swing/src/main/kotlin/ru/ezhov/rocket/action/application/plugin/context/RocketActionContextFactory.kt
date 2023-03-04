package ru.ezhov.rocket.action.application.plugin.context

import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.application.plugin.context.cache.DiskCacheService
import ru.ezhov.rocket.action.application.plugin.context.icon.ResourceIconService
import ru.ezhov.rocket.action.application.plugin.context.notification.PopupNotificationService

object RocketActionContextFactory {
    private val icon = ResourceIconService()
    private val notification = PopupNotificationService()
    private val cache = DiskCacheService()

    val context: RocketActionContext = object : RocketActionContext {
        override fun icon(): IconService = icon

        override fun notification(): NotificationService = notification

        override fun cache(): CacheService = cache
    }
}
