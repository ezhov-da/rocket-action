package ru.ezhov.rocket.action.application.core.application

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.InMemoryRocketActionComponentCache
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.Component
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JMenuItem

internal class RocketActionSettingsServiceEvaluationTest {
    @Test
    fun `should evaluate one setting once when it is both checked and created`() {
        val evaluations = AtomicInteger()
        val engineService = mockk<EngineService>()
        every { engineService.processWithEngine(any()) } answers {
            evaluations.incrementAndGet()
            "evaluated"
        }

        val model = RocketActionSettingsModel(
            id = ACTION_ID,
            type = TYPE,
            settings = listOf(SettingsModel(name = "label", value = "text")),
            actions = emptyList(),
            tags = emptyList(),
        )

        val cache = InMemoryRocketActionComponentCache()
        cache.add(ACTION_ID, RocketActionCached.newRocketAction(changedAction()))

        val newAction = unchangedAction()
        val factory = object : RocketActionFactoryUi {
            override fun create(
                settings: RocketActionSettings,
                context: RocketActionContext,
            ): RocketAction {
                // the create path reads the same settings the isChanged path already evaluated
                settings.settings()
                return newAction
            }

            override fun type(): RocketActionType = RocketActionType { TYPE }
        }
        val plugin = mockk<RocketActionPlugin>()
        every { plugin.factory(any()) } returns factory
        val pluginService = mockk<RocketActionPluginApplicationService>()
        every { pluginService.by(TYPE) } returns plugin

        val repository = mockk<RocketActionSettingsRepository>()
        every { repository.actions() } returns ActionsModel(actions = listOf(model))

        val contextFactory = mockk<RocketActionContextFactory>()
        every { contextFactory.context } returns mockk<RocketActionContext>(relaxed = true)

        val service = RocketActionSettingsService(
            rocketActionPluginApplicationService = pluginService,
            rocketActionSettingsRepository = repository,
            tagsService = mockk<TagsService>(relaxed = true),
            rocketActionContextFactory = contextFactory,
            engineService = engineService,
            rocketActionComponentCache = cache,
        )

        val components = service.loadAndGetAllComponents(service.newReloadScope())

        assertThat(components).hasSize(1)
        assertThat(evaluations.get()).isEqualTo(1)
    }

    private fun changedAction() = object : RocketAction {
        override fun contains(search: String): Boolean = false

        override fun isChanged(actionSettings: RocketActionSettings): Boolean {
            // the check path evaluates the settings first
            actionSettings.settings()
            return true
        }

        override fun component(): Component = JMenuItem("old")
    }

    private fun unchangedAction() = object : RocketAction {
        override fun contains(search: String): Boolean = false
        override fun isChanged(actionSettings: RocketActionSettings): Boolean = false
        override fun component(): Component = JMenuItem("new")
    }

    private companion object {
        const val ACTION_ID = "action-1"
        const val TYPE = "TEST"
    }
}
