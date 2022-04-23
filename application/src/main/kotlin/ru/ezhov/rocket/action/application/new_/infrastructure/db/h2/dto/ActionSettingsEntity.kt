package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto

import org.ktorm.entity.Entity
import java.util.UUID

interface ActionSettingsEntity : Entity<ActionSettingsEntity> {
    companion object : Entity.Factory<ActionSettingsEntity>()

    val id: UUID
    val name: String
    val value: String
}
