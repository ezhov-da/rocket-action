package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.schema

import org.ktorm.schema.Table
import org.ktorm.schema.text
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object ActionSettingsTable : Table<Nothing>("action_settings") {
    val id = uuid("id")
    val name = varchar("name")
    val value = text("value")
}
