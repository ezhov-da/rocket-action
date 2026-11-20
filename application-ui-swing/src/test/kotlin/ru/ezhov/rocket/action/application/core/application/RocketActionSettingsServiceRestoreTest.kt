package ru.ezhov.rocket.action.application.core.application

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.Component
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JMenu
import javax.swing.JMenuItem

internal class RocketActionSettingsServiceRestoreTest {
    private val cache = RocketActionComponentCacheFactory.cache
    private val created = AtomicInteger()
    private val context = mockk<RocketActionContext>(relaxed = true)

    @AfterEach
    fun cleanUp() {
        cache.clear()
    }

    @Test
    fun `should clear and refill groups at any nesting depth in catalog order`() {
        val service = service(
            group("g1", leaf("a"), group("g2", leaf("b"), group("g3", leaf("c")))),
        )
        assertThat(service.restoreMenuContent()).isNull()

        val content = service.loadAndGetAllComponents(service.newReloadScope())

        assertThat(content).hasSize(1)
        val g1 = content.single() as JMenu
        assertThat(g1.text).isEqualTo("g1")
        assertThat(titles(g1)).containsExactly("a", "g2")

        val g2 = g1.getItem(1) as JMenu
        assertThat(titles(g2)).containsExactly("b", "g3")

        val g3 = g2.getItem(1) as JMenu
        assertThat(titles(g3)).containsExactly("c")
    }

    @Test
    fun `should return a found nested entry to its group on restore`() {
        val service = service(group("g1", leaf("a"), group("g2", leaf("x"), leaf("c"))))
        val g1 = service.loadAndGetAllComponents(service.newReloadScope()).single() as JMenu
        val g2 = g1.getItem(1) as JMenu
        val c = g2.getItem(1)

        // a search shows the nested entry: the content menu becomes its only parent
        val searchView = JMenu()
        searchView.add(c)
        assertThat(g2.itemCount).isEqualTo(1)

        val restored = service.restoreMenuContent()!!

        assertThat(restored).containsExactly(g1)
        assertThat(titles(g2)).containsExactly("x", "c")
        assertThat(g2.getItem(1)).isSameAs(c)
        assertThat(searchView.itemCount).isZero()
    }

    @Test
    fun `should place a duplicated id only once`() {
        val service = service(
            group("g1", leaf("dup")),
            leaf("dup"),
        )

        val content = service.loadAndGetAllComponents(service.newReloadScope())

        // the first placement wins: the group keeps the entry, the top-level duplicate is skipped
        assertThat(content).hasSize(1)
        assertThat(titles(content.single() as JMenu)).containsExactly("dup")
    }

    @Test
    fun `should skip a cyclic reference with one warning per rebuild`() {
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        root.addAppender(appender)

        try {
            val service = service(group("a", group("b", group("a"))))

            val content = service.loadAndGetAllComponents(service.newReloadScope())

            val a = content.single() as JMenu
            val b = a.getItem(0) as JMenu
            assertThat(b.itemCount).isZero()
            assertThat(cyclicWarnings(appender)).hasSize(1)

            service.loadAndGetAllComponents(service.newReloadScope())
            assertThat(cyclicWarnings(appender)).hasSize(2)
        } finally {
            root.detachAppender(appender)
            appender.stop()
        }
    }

    @Test
    fun `should produce the complete menu after a reload that follows a search`() {
        val service = service(group("g1", leaf("a"), group("g2", leaf("c"))))
        val g1 = service.loadAndGetAllComponents(service.newReloadScope()).single() as JMenu
        val g2 = g1.getItem(1) as JMenu
        val c = g2.getItem(0)
        val createdBefore = created.get()

        // the search view took the nested entry out of its group
        val searchView = JMenu()
        searchView.add(c)

        val reloaded = service.loadAndGetAllComponents(service.newReloadScope())

        assertThat(reloaded).containsExactly(g1)
        assertThat(titles(g2)).containsExactly("c")
        assertThat(g2.getItem(0)).isSameAs(c)
        assertThat(searchView.itemCount).isZero()
        // the repair is a re-attachment: nothing is rebuilt
        assertThat(created.get()).isEqualTo(createdBefore)
    }

    private fun cyclicWarnings(appender: ListAppender<ILoggingEvent>): List<ILoggingEvent> =
        appender.list.filter { it.formattedMessage.contains(CYCLIC_WARNING) }

    private fun titles(menu: JMenu): List<String> =
        (0 until menu.itemCount).map { menu.getItem(it).text }

    private fun service(vararg actions: RocketActionSettingsModel): RocketActionSettingsService {
        val groupPlugin = GroupRocketActionUi().apply { factory(context) }
        val leafPlugin = leafPlugin()

        val pluginService = mockk<RocketActionPluginApplicationService>()
        every { pluginService.by(any()) } answers {
            when (firstArg<String>()) {
                GroupRocketActionUi.TYPE -> groupPlugin
                CHILD_TYPE -> leafPlugin
                else -> null
            }
        }

        val repository = mockk<RocketActionSettingsRepository>()
        every { repository.actions() } returns ActionsModel(actions = actions.toList())

        val contextFactory = mockk<RocketActionContextFactory>()
        every { contextFactory.context } returns context

        val engineService = mockk<EngineService>()
        every { engineService.processWithEngine(any()) } answers { firstArg<SettingsModel>().value }

        return RocketActionSettingsService(
            rocketActionPluginApplicationService = pluginService,
            rocketActionSettingsRepository = repository,
            tagsService = mockk<TagsService>(relaxed = true),
            rocketActionContextFactory = contextFactory,
            engineService = engineService,
            rocketActionComponentCache = cache,
        )
    }

    private fun leafPlugin(): RocketActionPlugin = object : RocketActionPlugin {
        override fun factory(context: RocketActionContext): RocketActionFactoryUi =
            object : RocketActionFactoryUi {
                override fun create(
                    settings: RocketActionSettings,
                    context: RocketActionContext,
                ): RocketAction {
                    created.incrementAndGet()
                    val item = JMenuItem(settings.id())
                    return object : RocketAction {
                        override fun contains(search: String): Boolean = false
                        override fun isChanged(actionSettings: RocketActionSettings): Boolean = false
                        override fun component(): Component = item
                    }
                }

                override fun type(): RocketActionType = RocketActionType { CHILD_TYPE }
            }

        override fun configuration(context: RocketActionContext): RocketActionConfiguration =
            mockk(relaxed = true)

        override fun info(): RocketActionPluginInfo = mockk(relaxed = true)
    }

    private fun group(id: String, vararg children: RocketActionSettingsModel) =
        RocketActionSettingsModel(
            id = id,
            type = GroupRocketActionUi.TYPE,
            settings = listOf(SettingsModel(name = "label", value = id)),
            actions = children.toList(),
            tags = emptyList(),
        )

    private fun leaf(id: String) = RocketActionSettingsModel(
        id = id,
        type = CHILD_TYPE,
        settings = emptyList(),
        actions = emptyList(),
        tags = emptyList(),
    )

    private companion object {
        const val CHILD_TYPE = "TEST"
        const val CYCLIC_WARNING = "Cyclic action reference detected"
    }
}
