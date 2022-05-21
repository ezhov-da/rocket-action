package ru.ezhov.rocket.action.core.domain.model

import ru.ezhov.rocket.action.api.RocketActionType

class ActionType(val value: String) {
    fun asRocketActionPlugin() = RocketActionType { value }
}
