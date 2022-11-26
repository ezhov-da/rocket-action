package ru.ezhov.rocket.action.application.plugin.context.notification

import ru.ezhov.rocket.action.api.context.notification.NotificationType

object PopupNotificationServiceTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val popupNotificationService = PopupNotificationService()
        for (i in 0..4) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            popupNotificationService.show(NotificationType.INFO, "" + i)
            popupNotificationService.show(NotificationType.WARN, "" + i)
            popupNotificationService.show(NotificationType.ERROR, "" + i)
        }
    }
}
