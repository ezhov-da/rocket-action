package ru.ezhov.rocket.action.api.context.notification

interface NotificationService {
    fun show(type: NotificationType, text: String)
}
