package ru.ezhov.rocket.action.application.new_.infrastructure.db

import arrow.core.getOrHandle
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import java.io.File

object LiquibaseDbPreparedService {
    fun prepare(factory: DbConnectionFactory) {
        val connection = factory
            .connection().getOrHandle { throw it }
        val database: Database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
        val liquibase = Liquibase(
            "dbchangelog.xml",
            FileSystemResourceAccessor(File("./../liquibase")),
            database
        )
        liquibase.update(Contexts("dev"), LabelExpression())
    }
}
