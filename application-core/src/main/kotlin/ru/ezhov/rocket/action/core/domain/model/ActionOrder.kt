package ru.ezhov.rocket.action.core.domain.model

@JvmInline
value class ActionOrder(val value: Int) {
    fun plusOne() = ActionOrder(value + 1)
}
