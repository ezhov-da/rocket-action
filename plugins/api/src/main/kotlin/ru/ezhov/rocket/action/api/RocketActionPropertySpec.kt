package ru.ezhov.rocket.action.api

import java.awt.Component

sealed class RocketActionPropertySpec(val defaultValue: String?) {
    class StringPropertySpec(defaultValue: String? = null) : RocketActionPropertySpec(defaultValue)

    class BooleanPropertySpec(defaultValue: Boolean? = null) :
        RocketActionPropertySpec(defaultValue = defaultValue?.toString())

    class IntPropertySpec(defaultValue: Int? = null, val min: Int = 0, val max: Int = 1000) :
        RocketActionPropertySpec(defaultValue = defaultValue?.toString())

    class ComponentPropertySpec(val component: PropertyComponent) : RocketActionPropertySpec(defaultValue = null)

    class ListPropertySpec(defaultValue: String? = null, val valuesForSelect: List<String> = emptyList()) :
        RocketActionPropertySpec(defaultValue)
}

interface PropertyComponent {
    fun component(): Component

    fun getPropertyValue(): String?

    fun setPropertyValueValue(value: String)
}
