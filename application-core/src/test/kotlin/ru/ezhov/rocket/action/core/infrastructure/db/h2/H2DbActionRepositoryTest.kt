package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.ezhov.rocket.action.core.domain.model.ActionIdSampleData
import ru.ezhov.rocket.action.core.domain.model.ActionOrder
import ru.ezhov.rocket.action.core.domain.model.ActionSampleData
import ru.ezhov.rocket.action.core.infrastructure.db.LiquibaseDbPreparedService

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

    @Test
    fun `should be get children successfully if parent null`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val actions = repo.children(null)
                .getOrHandle { throw it }

            assertThat(actions).hasSize(25)
        }
    }

    @Test
    fun `should save not exists action`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))
            val action = ActionSampleData.default(
                id = ActionIdSampleData.default(uuidAsString = "651cc1fc-c372-11ec-9d64-0242ac120002")
            )

            repo.addOrUpdate(listOf(action)).getOrHandle { throw it }

            val savedAction = repo.action(action.id).getOrHandle { throw it }
            assertThat(savedAction).isNotNull
        }
    }

    @Test
    fun `should update exists action`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))
            val action = ActionSampleData.default().withNewOrder(ActionOrder(5))

            repo.addOrUpdate(listOf(action)).getOrHandle { throw it }

            val savedAction = repo.action(action.id).getOrHandle { throw it }
            assertThat(savedAction!!.order.value).isEqualTo(5)
        }
    }

    @Test
    fun `should get all actions`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val actions = repo.all().getOrHandle { throw it }

            assertThat(actions).hasSize(30)
        }
    }

    @Test
    fun `should get actions by ids`() {
        with(LiquibaseDbPreparedService.prepareH2Db(tempFolder = tempFolder)) {
            val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default(factory = dbCredentialsFactory))

            val actions = repo.actions(listOf(
                ActionIdSampleData.default(uuidAsString = "de4a6ba8-c229-11ec-9d64-0242ac120002"),
                ActionIdSampleData.default(uuidAsString = "de7a6ba8-c229-11ec-9d64-0242ac120002"),
            )).getOrHandle { throw it }

            assertThat(actions).hasSize(2)
        }
    }
}
