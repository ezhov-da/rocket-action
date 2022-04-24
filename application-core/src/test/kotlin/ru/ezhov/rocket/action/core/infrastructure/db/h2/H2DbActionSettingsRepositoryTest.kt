package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.core.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.core.domain.model.ActionSettingName
import ru.ezhov.rocket.action.core.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.core.infrastructure.db.LiquibaseDbPreparedService

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

    @Test
    fun `should be save successfully`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val actionId =
                ActionIdSampleData.default(uuidAsString = "de2a6ba8-c229-11ec-9d64-0242ac120002")
            val actionSettingsFromRepo = repo.settings(actionId).getOrHandle { throw it }
            val name = ActionSettingName(value = "name123")
            val value = ActionSettingValue("value123")
            val newActionSettings = actionSettingsFromRepo!!.addOrChangeProperty(
                name = name,
                value = value,
            )
            repo.save(newActionSettings).getOrHandle { throw it }

            val actionSettingsFromRepoAfterSave = repo.settings(actionId).getOrHandle { throw it }
            assertThat(actionSettingsFromRepoAfterSave!!.value(name)!!.value).isEqualTo(value.value)
        }
    }
}
