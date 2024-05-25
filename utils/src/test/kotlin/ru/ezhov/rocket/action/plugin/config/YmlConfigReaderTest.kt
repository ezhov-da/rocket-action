package ru.ezhov.rocket.action.plugin.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class YmlConfigReaderTest {
    @Test
    fun `should valid when read config`() {
        val reader = this::class.java.getResourceAsStream("/test/config.yml").use {
            YmlConfigReader(it)
        }

        assertThat(reader.name()).isEqualTo("Open link")
        assertThat(reader.description()).isEqualTo("Link opening")
        assertThat(reader.nameBy("label")).isEqualTo("Заголовок для отображения")
        assertThat(reader.descriptionBy("label")).isEqualTo(
            "# Тестовое описание в MarkDown\nПробуем, что поделать\n"
        )
    }
}
