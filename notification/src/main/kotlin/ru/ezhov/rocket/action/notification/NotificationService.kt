package ru.ezhov.rocket.action.notification

interface NotificationService {
    fun show(type: NotificationType, text: String)
}