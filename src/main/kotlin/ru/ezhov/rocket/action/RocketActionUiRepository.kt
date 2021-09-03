package ru.ezhov.rocket.action

import ru.ezhov.rocket.action.api.RocketActionUi

interface RocketActionUiRepository {
    fun load()
    fun all(): List<RocketActionUi>
    fun by(type: String): RocketActionUi?
}