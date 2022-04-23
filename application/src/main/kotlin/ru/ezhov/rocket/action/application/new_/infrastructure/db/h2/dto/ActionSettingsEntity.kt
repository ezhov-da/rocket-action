package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto

import org.ktorm.entity.Entity
import java.util.UUID

interface ActionSettingsEntity : Entity<ActionSettingsEntity> {
    companion object : Entity.Factory<ActionSettingsEntity>()

    var id: UUID
    var name: String
    var value: String?
}
