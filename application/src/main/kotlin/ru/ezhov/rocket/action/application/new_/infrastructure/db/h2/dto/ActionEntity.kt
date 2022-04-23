package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import java.util.UUID

interface ActionEntity : Entity<ActionEntity> {
    companion object : Entity.Factory<ActionEntity>()

    val id: UUID
    val type: String
    val order: Int
    val creationDate: LocalDateTime
    val updateDate: LocalDateTime?
    val parentId: UUID?
}

