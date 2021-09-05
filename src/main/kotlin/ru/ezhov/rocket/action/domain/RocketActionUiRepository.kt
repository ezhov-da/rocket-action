package ru.ezhov.rocket.action.domain

import ru.ezhov.rocket.action.api.RocketActionFactoryUi

interface RocketActionUiRepository {
    fun all(): List<RocketActionFactoryUi>
    fun by(type: String): RocketActionFactoryUi?
}