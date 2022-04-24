package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto

import org.ktorm.entity.Entity
import java.time.LocalDateTime
import java.util.UUID

interface ActionEntity : Entity<ActionEntity> {
    companion object : Entity.Factory<ActionEntity>()

    var id: UUID
    var type: String
    var order: Int
    var creationDate: LocalDateTime
    var updateDate: LocalDateTime?
    var parentId: UUID?
}

