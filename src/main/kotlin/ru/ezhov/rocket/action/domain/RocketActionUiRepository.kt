package ru.ezhov.rocket.action.domain

import ru.ezhov.rocket.action.api.RocketActionUi

interface RocketActionUiRepository {
    fun all(): List<RocketActionUi>
    fun by(type: String): RocketActionUi?
}