package ru.ezhov.rocket.action.properties

import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.io.IOException
import java.util.*

class ResourceGeneralPropertiesRepository : GeneralPropertiesRepository {
    override fun all(): Properties {
        val properties = Properties()
        try {
            properties.load(this.javaClass.getResourceAsStream("/general.properties"))
        } catch (e: IOException) {
            e.printStackTrace()
            NotificationFactory.notification.show(NotificationType.ERROR, "Error read general properties")
        }
        return properties
    }
}