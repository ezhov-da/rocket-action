package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.getOrHandle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class H2DbActionSettingsRepositoryTest {

    @Test
    fun `should be get all action settings when get all`() {
        val repo = H2DbActionSettingsRepository(H2DbConnectionFactorySampleData.factory())

        val all = repo.all().getOrHandle { throw it }

        assertThat(all).hasSize(29)
    }
}
