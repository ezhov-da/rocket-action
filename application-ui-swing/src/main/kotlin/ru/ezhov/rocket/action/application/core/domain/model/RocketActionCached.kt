package ru.ezhov.rocket.action.application.core.domain.model

import ru.ezhov.rocket.action.api.RocketAction

data class RocketActionCached(
    val origin: RocketAction,
    val state: RocketActionCachedState,
) {
    companion object {
        fun newRocketAction(origin: RocketAction): RocketActionCached =
            RocketActionCached(origin, RocketActionCachedState.CHANGED_SINCE_LAST_LOAD)
    }

    fun toNotChanged(): RocketActionCached =
        RocketActionCached(origin, RocketActionCachedState.NOT_CHANGED_SINCE_LAST_LOAD)
}

enum class RocketActionCachedState {
    CHANGED_SINCE_LAST_LOAD, NOT_CHANGED_SINCE_LAST_LOAD
}
