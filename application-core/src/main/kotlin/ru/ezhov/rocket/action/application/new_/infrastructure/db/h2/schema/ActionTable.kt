package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.schema

import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionEntity

object ActionTable : Table<ActionEntity>("action") {
    val id = uuid("id").primaryKey().bindTo { it.id }
    val type = varchar("type").bindTo { it.type }
    val creationDate = datetime("creation_date").bindTo { it.creationDate }
    val updateDate = datetime("update_date").bindTo { it.updateDate }
    val order = int("sequence_order").bindTo { it.order }
    val parentId = uuid("parent_id").bindTo { it.parentId }
}
