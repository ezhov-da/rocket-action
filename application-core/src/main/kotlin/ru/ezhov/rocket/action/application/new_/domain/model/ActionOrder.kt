package ru.ezhov.rocket.action.application.new_.domain.model

@JvmInline
value class ActionOrder(val value: Int) {
    fun plusOne() = ActionOrder(value + 1)
}
