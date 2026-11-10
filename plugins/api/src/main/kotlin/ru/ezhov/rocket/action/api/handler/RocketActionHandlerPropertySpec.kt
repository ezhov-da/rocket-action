package ru.ezhov.rocket.action.api.handler

sealed class RocketActionHandlerPropertySpec(val defaultValue: String?) {
    class StringPropertySpec(defaultValue: String? = null) : RocketActionHandlerPropertySpec(defaultValue)

    class BooleanPropertySpec(defaultValue: Boolean? = null) : RocketActionHandlerPropertySpec(defaultValue = defaultValue?.toString())

    class IntPropertySpec(defaultValue: Int? = null, val min: Int = 0, val max: Int = 1000) : RocketActionHandlerPropertySpec(defaultValue = defaultValue?.toString())
}
