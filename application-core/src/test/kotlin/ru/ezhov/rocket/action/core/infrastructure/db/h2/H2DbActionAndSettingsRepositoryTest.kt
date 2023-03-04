package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.util.Files
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.core.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.core.domain.model.ActionSettingName
import ru.ezhov.rocket.action.core.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.core.domain.model.NewActionSampleData
import ru.ezhov.rocket.action.core.infrastructure.db.LiquibaseDbPreparedService

class H2DbActionAndSettingsRepositoryTest {

    @Test
    fun `should be save successfully`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = Files.newTemporaryFolder())) {
            val repoAction = H2DbActionRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repoActionSettings = H2DbActionSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )
            val repo = H2DbActionAndSettingsRepository(
                factory = H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory)
            )

            val newAction =
                NewActionSampleData.default(map = mapOf(ActionSettingName("name") to ActionSettingValue("value")))
            repo.add(newAction).getOrHandle { throw (it) }

            val actionFromRepo = repoAction.action(newAction.id).getOrHandle { throw it }
            val actionSettingsFromRepo = repoActionSettings.settings(newAction.id).getOrHandle { throw it }
            assertThat(actionFromRepo).isNotNull
            assertThat(actionSettingsFromRepo).isNotNull
        }
    }

    @Test
    fun `should be remove recursive`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = Files.newTemporaryFolder())) {
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
