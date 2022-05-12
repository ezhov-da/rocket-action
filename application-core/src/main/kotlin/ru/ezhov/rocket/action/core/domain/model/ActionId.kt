package ru.ezhov.rocket.action.core.domain.model

import java.util.UUID

@JvmInline
value class ActionId(val value: UUID) {
    companion object {
        fun of(uuid: String) = ActionId(UUID.fromString(uuid))

        fun create() = ActionId(UUID.randomUUID())
    }
}
