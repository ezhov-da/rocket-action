package ru.ezhov.rocket.action.application.plugin.context.notification

import ru.ezhov.rocket.action.api.context.notification.NotificationService

object NotificationFactory {
    val notification: NotificationService = PopupNotificationService()
}
