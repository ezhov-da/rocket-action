package ru.ezhov.rocket.action.core.infrastructure.db.h2.dto

import org.ktorm.entity.Entity
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import java.util.UUID

interface ActionSettingsEntity : Entity<ActionSettingsEntity> {
    companion object : Entity.Factory<ActionSettingsEntity>()

    var id: UUID
    var name: String
    var value: String?
}

private fun ActionSettings.toDbModel(): List<ActionSettingsEntity> {
    val actionId = this.id.value
    return this.map.map { aso ->
        ActionSettingsEntity {
            this.id = actionId
            this.name = aso.key.value
            this.value = aso.value?.value
        }
    }
}
