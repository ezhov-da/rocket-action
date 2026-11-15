package ru.ezhov.rocket.action.application.core.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCachedState
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.Component
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.SwingUtilities

internal class RocketActionSettingsServiceReloadTest {
    private val cache = RocketActionComponentCacheFactory.cache

    @AfterEach
    fun cleanUp() {
        cache.clear()
    }

    @Test
    fun `should recreate a group only when its child was rebuilt`() {
        val childChanged = AtomicBoolean(false)
        val groupCreates = AtomicInteger()
        val context = mockk<RocketActionContext>(relaxed = true)

        val groupPlugin = GroupRocketActionUi().apply { factory(context) }
        val countingGroupPlugin = object : RocketActionPlugin {
            override fun factory(context: RocketActionContext): RocketActionFactoryUi =
                object : RocketActionFactoryUi {
                    override fun create(
                        settings: RocketActionSettings,
                        context: RocketActionContext,
                    ): RocketAction? {
                        groupCreates.incrementAndGet()
                        return groupPlugin.create(settings, context)
                    }

                    override fun type(): RocketActionType = RocketActionType { GroupRocketActionUi.TYPE }
                }

            override fun configuration(context: RocketActionContext): RocketActionConfiguration =
                groupPlugin.configuration(context)

            override fun info(): RocketActionPluginInfo = object : RocketActionPluginInfo {
                override fun version(): String = "test"
                override fun author(): String = "test"
                override fun link(): String? = null
            }
        }

        val childPlugin = object : RocketActionPlugin {
            override fun factory(context: RocketActionContext): RocketActionFactoryUi =
                object : RocketActionFactoryUi {
                    override fun create(
                        settings: RocketActionSettings,
                        context: RocketActionContext,
                    ): RocketAction = object : RocketAction {
                        override fun contains(search: String): Boolean = false
                        override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                            childChanged.get()

                        override fun component(): Component = JMenuItem("child")
                    }

                    override fun type(): RocketActionType = RocketActionType { CHILD_TYPE }
                }

            override fun configuration(context: RocketActionContext): RocketActionConfiguration =
                mockk(relaxed = true)

            override fun info(): RocketActionPluginInfo = mockk(relaxed = true)
        }

        val service = service(
            actions = listOf(groupWithChild()),
            context = context,
            pluginBy = { type ->
                when (type) {
                    GroupRocketActionUi.TYPE -> countingGroupPlugin
                    CHILD_TYPE -> childPlugin
                    else -> null
                }
            },
        )

        service.loadAndGetAllComponents(service.newReloadScope())
        assertThat(groupCreates.get()).isEqualTo(1)

        // nothing changed: the group and the child are reused
        service.loadAndGetAllComponents(service.newReloadScope())
        assertThat(groupCreates.get()).isEqualTo(1)
        assertThat(cache.by(CHILD_ID)?.state).isEqualTo(RocketActionCachedState.NOT_CHANGED_SINCE_LAST_LOAD)

        // the child is rebuilt: the group must be rebuilt as well
        childChanged.set(true)
        service.loadAndGetAllComponents(service.newReloadScope())
        assertThat(groupCreates.get()).isEqualTo(2)
    }

    @Test
    fun `should isolate failures and show one aggregated notification`() {
        val notificationService = mockk<NotificationService>(relaxed = true)
        val context = mockk<RocketActionContext>(relaxed = true)
        every { context.notification() } returns notificationService

        val plugin = object : RocketActionPlugin {
            override fun factory(context: RocketActionContext): RocketActionFactoryUi =
                object : RocketActionFactoryUi {
                    override fun create(
                        settings: RocketActionSettings,
                        context: RocketActionContext,
                    ): RocketAction {
                        if (settings.id().startsWith("bad")) {
                            throw IllegalStateException("broken action '${settings.id()}'")
                        }

                        return object : RocketAction {
                            override fun contains(search: String): Boolean = false
                            override fun isChanged(actionSettings: RocketActionSettings): Boolean = false
                            override fun component(): Component = JMenuItem(settings.id())
                        }
                    }

                    override fun type(): RocketActionType = RocketActionType { CHILD_TYPE }
                }

            override fun configuration(context: RocketActionContext): RocketActionConfiguration =
                mockk(relaxed = true)

            override fun info(): RocketActionPluginInfo = mockk(relaxed = true)
        }

        val service = service(
            actions = listOf(action("good"), action("bad-1"), action("bad-2")),
            context = context,
            pluginBy = { if (it == CHILD_TYPE) plugin else null },
        )

        val components = service.loadAndGetAllComponents(service.newReloadScope())

        // the EDT notification is queued by the reload
        SwingUtilities.invokeAndWait { }

        assertThat(components).hasSize(1)
        verify(exactly = 1) {
            notificationService.show(
                NotificationType.ERROR,
                match { message -> message.contains("2") },
            )
        }
    }

    @Test
    fun `should keep the phases and the work stable across three consecutive reloads`() {
        val engineCalls = AtomicInteger()
        val created = AtomicInteger()
        val context = mockk<RocketActionContext>(relaxed = true)

        val leafPlugin = object : RocketActionPlugin {
            override fun factory(context: RocketActionContext): RocketActionFactoryUi =
                object : RocketActionFactoryUi {
                    override fun create(
                        settings: RocketActionSettings,
                        context: RocketActionContext,
                    ): RocketAction {
                        created.incrementAndGet()
                        // real plugins read the settings, which evaluates the templates
                        val label = settings.settings()["label"]
                        return object : RocketAction {
                            override fun contains(search: String): Boolean =
                                label?.contains(search) ?: false

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                actionSettings.settings()["label"] != label

                            override fun component(): Component = JMenuItem(label)
                        }
                    }

                    override fun type(): RocketActionType = RocketActionType { CHILD_TYPE }
                }

            override fun configuration(context: RocketActionContext): RocketActionConfiguration =
                mockk(relaxed = true)

            override fun info(): RocketActionPluginInfo = mockk(relaxed = true)
        }
        val groupPlugin = GroupRocketActionUi().apply { factory(context) }

        val service = service(
            actions = listOf(groupOfTemplates(), templateLeaf("t1"), templateLeaf("t2"), templateLeaf("t3")),
            context = context,
            pluginBy = { type ->
                when (type) {
                    GroupRocketActionUi.TYPE -> groupPlugin
                    CHILD_TYPE -> leafPlugin
                    else -> null
                }
            },
            onEvaluate = { engineCalls.incrementAndGet() },
        )

        val reloads = (1..3).map {
            val engineCallsBefore = engineCalls.get()
            val createdBefore = created.get()

            val scope = service.newReloadScope()
            service.loadAndGetAllComponents(scope)
            // the UI reports the swap, the variables load and the MCP refresh into the same scope
            scope.variablesLoaded(0)
            scope.swapped(0)
            scope.mcpFinished(1)

            Reload(
                scope = scope,
                engineCalls = engineCalls.get() - engineCallsBefore,
                created = created.get() - createdBefore,
            )
        }

        val first = reloads.first()

        // the first reload builds the three leaves, the two following ones reuse them
        assertThat(first.created).isEqualTo(5)
        assertThat(reloads.drop(1).map { it.created }).containsOnly(0)

        // the settings of an action are evaluated once per reload, not once per consumer
        assertThat(reloads.map { it.engineCalls }.distinct()).hasSize(1)
        assertThat(first.engineCalls).isPositive()

        reloads.forEach { reload ->
            val scope = reload.scope

            assertThat(scope.actions).isEqualTo(6)
            assertThat(scope.groups).isEqualTo(1)
            assertThat(scope.failures).isZero()
            assertThat(scope.evaluations).isEqualTo(first.scope.evaluations)
            assertThat(scope.evaluations).isPositive()

            assertThat(scope.settingsReadMs).isBetween(0L, RELOAD_BUDGET_MS)
            assertThat(scope.cacheFillMs).isBetween(0L, RELOAD_BUDGET_MS)
            assertThat(scope.componentsMs).isBetween(0L, RELOAD_BUDGET_MS)
            assertThat(scope.swapMs).isBetween(0L, RELOAD_BUDGET_MS)
            assertThat(scope.variablesMs).isBetween(0L, RELOAD_BUDGET_MS)
            assertThat(scope.totalMs()).isBetween(0L, RELOAD_BUDGET_MS)

            val line = scope.render()
            ActionReloadScope.METRIC_KEYS.forEach { key ->
                assertThat(line).describedAs("phase key '%s'", key).contains("$key=")
            }
            assertThat(line).doesNotContain("=-1ms")
            assertThat(line).contains("evaluations=${first.scope.evaluations}")
        }
    }

    private data class Reload(
        val scope: ActionReloadScope,
        val engineCalls: Int,
        val created: Int,
    )

    private fun service(
        actions: List<RocketActionSettingsModel>,
        context: RocketActionContext,
        pluginBy: (String) -> RocketActionPlugin?,
        onEvaluate: () -> Unit = {},
    ): RocketActionSettingsService {
        val pluginService = mockk<RocketActionPluginApplicationService>()
        every { pluginService.by(any()) } answers { pluginBy(firstArg<String>()) }

        val repository = mockk<RocketActionSettingsRepository>()
        every { repository.actions() } returns ActionsModel(actions = actions)

        val contextFactory = mockk<RocketActionContextFactory>()
        every { contextFactory.context } returns context

        val engineService = mockk<EngineService>()
        every { engineService.processWithEngine(any()) } answers {
            onEvaluate()
            firstArg<SettingsModel>().value
        }

        return RocketActionSettingsService(
            rocketActionPluginApplicationService = pluginService,
            rocketActionSettingsRepository = repository,
            tagsService = mockk<TagsService>(relaxed = true),
            rocketActionContextFactory = contextFactory,
            engineService = engineService,
            rocketActionComponentCache = cache,
        )
    }

    private fun groupWithChild() = RocketActionSettingsModel(
        id = GROUP_ID,
        type = GroupRocketActionUi.TYPE,
        settings = listOf(SettingsModel(name = "label", value = "Group")),
        actions = listOf(action(CHILD_ID)),
        tags = emptyList(),
    )

    private fun action(id: String) = RocketActionSettingsModel(
        id = id,
        type = CHILD_TYPE,
        settings = emptyList(),
        actions = emptyList(),
        tags = emptyList(),
    )

    private fun templateLeaf(id: String) = RocketActionSettingsModel(
        id = id,
        type = CHILD_TYPE,
        settings = listOf(
            SettingsModel(name = "label", value = "Leaf $id"),
            SettingsModel(name = "text", value = "template('$id')", valueType = SettingsValueType.GROOVY_TEMPLATE),
        ),
        actions = emptyList(),
        tags = emptyList(),
    )

    private fun groupOfTemplates() = RocketActionSettingsModel(
        id = GROUP_ID,
        type = GroupRocketActionUi.TYPE,
        settings = listOf(SettingsModel(name = "label", value = "Group")),
        actions = listOf(templateLeaf("inside-1"), templateLeaf("inside-2")),
        tags = emptyList(),
    )

    private companion object {
        const val GROUP_ID = "group"
        const val CHILD_ID = "child"
        const val CHILD_TYPE = "TEST"
        const val RELOAD_BUDGET_MS = 5_000L
    }
}
