package ru.ezhov.rocket.action.application.properties

interface GeneralPropertiesRepository {
    fun asString(name: UsedPropertiesName, default: String): String
    fun asInteger(name: UsedPropertiesName, default: Int): Int
    fun asLong(name: UsedPropertiesName, default: Long): Long
    fun asBoolean(name: UsedPropertiesName, default: Boolean): Boolean
    fun asFloat(name: UsedPropertiesName, default: Float): Float
    fun asDouble(name: UsedPropertiesName, default: Double): Double

    fun asStringOrNull(name: UsedPropertiesName): String?
    fun asIntegerOrNull(name: UsedPropertiesName): Int?
    fun asLongOrNull(name: UsedPropertiesName): Long?
}
