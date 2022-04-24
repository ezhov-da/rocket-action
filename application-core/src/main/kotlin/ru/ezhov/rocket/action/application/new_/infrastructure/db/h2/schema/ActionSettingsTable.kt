package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.schema

import org.ktorm.schema.Table
import org.ktorm.schema.text
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionSettingsEntity

object ActionSettingsTable : Table<ActionSettingsEntity>("action_settings") {
    val id = uuid("id").bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val value = text("data").bindTo { it.value }
}
