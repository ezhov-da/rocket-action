package ru.ezhov.rocket.action.core.infrastructure.db

import arrow.core.getOrHandle
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import mu.KotlinLogging
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.core.infrastructure.db.h2.H2DbCredentialsFactorySampleData
import java.io.File

private val logger = KotlinLogging.logger {}

class LiquibaseDbPreparedService private constructor(
    private val factory: DbConnectionFactory,
    val dbCredentialsFactory: DbCredentialsFactory
) {

    companion object {
        fun prepareH2Db(tempFolder: TemporaryFolder): LiquibaseDbPreparedService {
            val tempFile = tempFolder.newFile();
            logger.debug { "Test db=${tempFile.absolutePath}" }
            val credentialFactory = H2DbCredentialsFactorySampleData.from(tempFile)

            return LiquibaseDbPreparedService(
                factory = ConnectionFactorySampleData.driverManager(factory = credentialFactory),
                dbCredentialsFactory = credentialFactory
            ).also {
                it.prepare()
            }
        }
    }

    private fun prepare() {
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
