package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import ru.ezhov.rocket.action.application.new_.domain.model.ActionIdSampleData

class H2DbActionRepositoryTest {
    @Test
    fun `should be found action when get action by id`() {
        val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default())

        val action = repo.action(ActionIdSampleData.default()).getOrHandle { throw it }

        assertThat(action!!.id.value).isEqualTo(ActionIdSampleData.default().value)
    }

    @Test
    fun `should be not found action when get action by id`() {
        val repo = H2DbActionRepository(H2DbKtormDbConnectionFactorySampleData.default())

        val action = repo.action(ActionIdSampleData.default("de1a6ba8-c229-11ec-9d64-0242ac12000a"))
            .getOrHandle { throw it }

        assertThat(action).isNull()
    }
}
