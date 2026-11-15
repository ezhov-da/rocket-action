package ru.ezhov.rocket.action.application.core.domain.model

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import ru.ezhov.rocket.action.application.core.domain.EngineService
import java.util.concurrent.atomic.AtomicInteger

internal class RocketActionSettingsModelTest {
    @Test
    fun `should evaluate settings once for four reads of the same view`() {
        val calls = AtomicInteger()
        val scope = ActionReloadScope {
            calls.incrementAndGet()
            "evaluated"
        }
        val view = model(valueType = SettingsValueType.GROOVY_TEMPLATE).to(scope)

        repeat(4) {
            assertThat(view.settings()).containsEntry("label", "evaluated")
        }

        assertThat(calls.get()).isEqualTo(1)
        assertThat(scope.evaluations).isEqualTo(1)
    }

    @Test
    fun `should count only template-backed settings as evaluations`() {
        val scope = ActionReloadScope { "evaluated" }

        model().to(scope).settings()

        assertThat(scope.evaluations).isZero()
    }

    @Test
    fun `should reuse one evaluation across isChanged and create views`() {
        val calls = AtomicInteger()
        val scope = ActionReloadScope {
            calls.incrementAndGet()
            "evaluated"
        }

        model().to(scope).settings()
        model().to(scope).settings()

        assertThat(calls.get()).isEqualTo(1)
    }

    @Test
    fun `should memoise nested actions in the view`() {
        val calls = AtomicInteger()
        val scope = ActionReloadScope {
            calls.incrementAndGet()
            "evaluated"
        }
        val parent = RocketActionSettingsModel(
            id = "parent",
            type = "TEST",
            settings = listOf(SettingsModel(name = "label", value = "parent")),
            actions = listOf(model()),
            tags = emptyList(),
        )

        val view = parent.to(scope)
        assertThat(view.actions()).hasSize(1)
        assertThat(view.actions()).hasSize(1)
        view.actions().first().settings()
        view.actions().first().settings()

        // the nested view and its settings are created once
        assertThat(view.actions().first()).isSameAs(view.actions().first())
        assertThat(calls.get()).isEqualTo(1)
    }

    @Test
    fun `should keep the compatibility overload working`() {
        val engineService = mockk<EngineService>()
        every { engineService.processWithEngine(any()) } returns "from-engine"

        val view = model().to(engineService)

        assertThat(view.settings()).containsEntry("label", "from-engine")
    }

    private fun model(
        valueType: SettingsValueType? = null,
    ) = RocketActionSettingsModel(
        id = "action-1",
        type = "TEST",
        settings = listOf(SettingsModel(name = "label", value = "text", valueType = valueType)),
        actions = emptyList(),
        tags = emptyList(),
    )
}
