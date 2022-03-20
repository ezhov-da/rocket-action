package ru.ezhov.rocket.action.api

sealed class RocketActionPropertySpec(val defaultValue: String?) {
    class StringPropertySpec(defaultValue: String? = null) : RocketActionPropertySpec(defaultValue)

    class BooleanPropertySpec(defaultValue: Boolean? = null) : RocketActionPropertySpec(defaultValue = defaultValue?.toString())

    class ListPropertySpec(defaultValue: String? = null, val valuesForSelect: List<String> = emptyList())
        : RocketActionPropertySpec(defaultValue)
}