package ru.ezhov.rocket.action.core.infrastructure.db.h2

import org.ktorm.database.Database
import org.ktorm.entity.sequenceOf
import ru.ezhov.rocket.action.core.infrastructure.db.h2.schema.ActionSettingsTable
import ru.ezhov.rocket.action.core.infrastructure.db.h2.schema.ActionTable

val Database.actions get() = this.sequenceOf(ActionTable)
val Database.actionSettings get() = this.sequenceOf(ActionSettingsTable)
