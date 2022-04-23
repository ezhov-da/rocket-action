package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.application.new_.infrastructure.db.LiquibaseDbPreparedService

class H2DbActionSettingsRepositoryTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should be get all action settings when get all`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )

            val all = repo.all().getOrHandle { throw it }

            assertThat(all).hasSize(29)
        }
    }

    @Test
    fun `should be found action settings by id`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val id = ActionIdSampleData.default(uuidAsString = "de2a6ba8-c229-11ec-9d64-0242ac120002")

            val actionSettings = repo.settings(id).getOrHandle { throw it }

            assertThat(actionSettings).isNotNull.extracting { it?.id }.isEqualTo(id)
        }
    }
}
