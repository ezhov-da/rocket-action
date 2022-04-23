package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbCredentialsFactory
import ru.ezhov.rocket.action.application.new_.infrastructure.db.LiquibaseDbPreparedService

private val logger = KotlinLogging.logger {}

class H2DbActionSettingsRepositoryTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should be get all action settings when get all`() {
        val tempFile = tempFolder.newFile();
        logger.debug { "Test db=${tempFile.absolutePath}" }

        val credentialFactory = object : DbCredentialsFactory {
            override val url: String
                get() = "jdbc:h2:${tempFile.absolutePath}"
            override val user: String
                get() = "test"
            override val password: String
                get() = "test"
        }
        LiquibaseDbPreparedService.prepare(H2DbConnectionFactorySampleData.default(factory = credentialFactory))

        val repo = H2DbActionSettingsRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = credentialFactory))

        val all = repo.all().getOrHandle { throw it }

        assertThat(all).hasSize(29)
    }
}
