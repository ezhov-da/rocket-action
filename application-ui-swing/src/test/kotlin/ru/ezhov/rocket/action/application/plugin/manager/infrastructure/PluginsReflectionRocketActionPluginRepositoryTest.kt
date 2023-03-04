package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PluginsReflectionRocketActionPluginRepositoryTest {
    @Test
    fun `should load plugins`() {
        val repo = PluginsReflectionRocketActionPluginRepository()
        val list = repo.all()

        assertThat(list).isNotEmpty
    }
}
