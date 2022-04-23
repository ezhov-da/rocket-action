package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.schema

import org.ktorm.schema.Table
import org.ktorm.schema.datetime
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object ActionTable: Table<Nothing> ("action"){
    val id = uuid("id").primaryKey()
    val type = varchar("type")
    val creationDate = datetime("creation_date")
    val updateDate = datetime("update_date")
    val order = int("order")
    val parentId = uuid("type")
}
