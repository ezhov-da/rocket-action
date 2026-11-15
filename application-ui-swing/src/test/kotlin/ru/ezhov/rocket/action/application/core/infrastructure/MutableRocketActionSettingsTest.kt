package ru.ezhov.rocket.action.application.core.infrastructure

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import java.util.concurrent.atomic.AtomicInteger

internal class MutableRocketActionSettingsTest {
    @Test
    fun `should evaluate settings once for two reads`() {
        val calls = AtomicInteger()
        val engineService = mockk<EngineService>()
        every { engineService.processWithEngine(any()) } answers {
            calls.incrementAndGet()
            "evaluated"
        }

        val settings = MutableRocketActionSettings(
            id = "action-1",
            type = "TEST",
            settings = mutableListOf(SettingsModel(name = "label", value = "text")),
            tags = emptyList(),
        )

        val view = settings.to(engineService)

        assertThat(view.settings()).containsEntry("label", "evaluated")
        assertThat(view.settings()).containsEntry("label", "evaluated")
        assertThat(calls.get()).isEqualTo(1)
    }
}
