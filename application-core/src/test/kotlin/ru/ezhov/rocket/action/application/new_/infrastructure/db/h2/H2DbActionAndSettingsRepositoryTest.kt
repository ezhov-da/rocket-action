package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSampleData
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingsSampleData
import ru.ezhov.rocket.action.application.new_.infrastructure.db.LiquibaseDbPreparedService

class H2DbActionAndSettingsRepositoryTest {
    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should be save successfully`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repoAction = H2DbActionRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repoActionSettings = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repo = H2DbActionAndSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )

            val id = ActionIdSampleData.default(uuidAsString = "a19c352e-c375-11ec-9d64-0242ac120002")
            val newAction = ActionSampleData.default(id = id)
            val newActionSettings = ActionSettingsSampleData.default(id = id)

            repo.add(newAction, newActionSettings).getOrHandle { throw (it) }

            val actionFromRepo = repoAction.action(id).getOrHandle { throw it }
            val actionSettingsFromRepo = repoActionSettings.settings(id).getOrHandle { throw it }
            assertThat(actionFromRepo).isNotNull
            assertThat(actionSettingsFromRepo).isNotNull
        }
    }

    @Test
    fun `should be remove recursive`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repoAction = H2DbActionRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repoActionSettings = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repo = H2DbActionAndSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )

            val baseId = ActionIdSampleData.default(uuidAsString = "de16aba8-c229-11ec-9d64-0242ac120002")
            val childId = ActionIdSampleData.default(uuidAsString = "de17aba8-c229-11ec-9d64-0242ac120002")
            repo.remove(id = baseId, withAllChildrenRecursive = true).getOrHandle { throw (it) }

            val baseActionFromRepo = repoAction.action(baseId).getOrHandle { throw it }
            val baseActionSettingsFromRepo = repoActionSettings.settings(baseId).getOrHandle { throw it }
            assertThat(baseActionFromRepo).isNull()
            assertThat(baseActionSettingsFromRepo).isNull()

            val childActionFromRepo = repoAction.action(childId).getOrHandle { throw it }
            val childActionSettingsFromRepo = repoActionSettings.settings(childId).getOrHandle { throw it }
            assertThat(childActionFromRepo).isNull()
            assertThat(childActionSettingsFromRepo).isNull()
        }
    }
}
