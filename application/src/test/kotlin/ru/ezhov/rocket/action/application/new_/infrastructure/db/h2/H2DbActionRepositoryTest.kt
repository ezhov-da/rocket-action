package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.application.new_.infrastructure.db.LiquibaseDbPreparedService

class H2DbActionRepositoryTest {

    @Rule
    @JvmField
    val tempFolder = TemporaryFolder()

    @Test
    fun `should be found action when get action by id`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val action = repo.action(ActionIdSampleData.default()).getOrHandle { throw it }

            assertThat(action!!.id.value).isEqualTo(ActionIdSampleData.default().value)
        }
    }

    @Test
    fun `should be not found action when get action by id`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val action = repo.action(ActionIdSampleData.default("de1a6ba8-c229-11ec-9d64-0242ac12000a"))
                .getOrHandle { throw it }

            assertThat(action).isNull()
        }
    }

    @Test
    fun `should be get children successfully`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val actions = repo.children(ActionIdSampleData.default("de16aba8-c229-11ec-9d64-0242ac120002"))
                .getOrHandle { throw it }

            assertThat(actions).hasSize(3)
        }
    }
}
