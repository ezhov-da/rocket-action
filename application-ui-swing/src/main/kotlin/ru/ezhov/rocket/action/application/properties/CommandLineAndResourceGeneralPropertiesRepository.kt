package ru.ezhov.rocket.action.application.properties

import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import java.util.*

private val logger = KotlinLogging.logger { }

@Component
class CommandLineAndResourceGeneralPropertiesRepository(
    private val notificationService: NotificationService
) : GeneralPropertiesRepository {

    private val properties: Properties = Properties()

    init {
        try {
            properties.load(this.javaClass.getResourceAsStream("/general.properties"))
        } catch (e: Exception) {
            "Error read general properties"
                .let { text ->
                    logger.error(e) { text }
                    notificationService.show(type = NotificationType.ERROR, text = text)
                }
        }
    }

    override fun asString(name: UsedPropertiesName, default: String): String =
        try {
            System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName)
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asInteger(name: UsedPropertiesName, default: Int): Int =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toInt()
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asLong(name: UsedPropertiesName, default: Long): Long =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toLong()
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asBoolean(name: UsedPropertiesName, default: Boolean): Boolean =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toBoolean()
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asFloat(name: UsedPropertiesName, default: Float): Float =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toFloat()
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asDouble(name: UsedPropertiesName, default: Double): Double =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toDouble()
                ?: default
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            default
        }

    override fun asStringOrNull(name: UsedPropertiesName): String? =
        try {
            System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName)
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            null
        }

    override fun asIntegerOrNull(name: UsedPropertiesName): Int? =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toInt()
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            null
        }

    override fun asLongOrNull(name: UsedPropertiesName): Long? =
        try {
            (System.getProperty(name.propertyName)
                ?: properties.getProperty(name.propertyName))?.toLong()
        } catch (ex: Exception) {
            logger.warn(ex) { "Error read property=$name" }
            null
        }
}
