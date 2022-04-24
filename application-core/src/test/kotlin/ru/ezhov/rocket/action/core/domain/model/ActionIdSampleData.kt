package ru.ezhov.rocket.action.core.domain.model

import java.util.UUID

object ActionIdSampleData {
    fun default(
        uuidAsString: String = "de1a6ba8-c229-11ec-9d64-0242ac120002"
    ) = ActionId(UUID.fromString(uuidAsString))

    fun new() = ActionId(UUID.randomUUID())
}
