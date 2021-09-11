package ru.ezhov.rocket.action.domain

import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionType

interface RocketActionUiRepository {
    fun all(): List<RocketActionFactoryUi>

    fun by(type: RocketActionType): RocketActionFactoryUi?
}