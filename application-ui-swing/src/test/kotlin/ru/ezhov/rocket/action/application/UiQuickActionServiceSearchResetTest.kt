package ru.ezhov.rocket.action.application

import io.mockk.every
import io.mockk.mockk
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
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ActionExecuteStatusPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.base.ChainBasePanel
import ru.ezhov.rocket.action.application.configuration.ui.ConfigurationFrameFactory
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.search.application.SearchService
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.application.tags.application.TagAndKeys
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.ui.color.ColorConstants
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JTextField
import javax.swing.SwingUtilities

internal class UiQuickActionServiceSearchResetTest {
    private val cache = RocketActionComponentCacheFactory.cache
    private val subscribers = mutableListOf<DomainEventSubscriber>()

    @AfterEach
    fun cleanUp() {
        subscribers.forEach { DomainEventFactory.subscriberRegistrar.unsubscribe(it) }
        subscribers.clear()
        cache.clear()
    }

    @Test
    fun `should restore the menu on Esc and reset the search field`() {
        val fixture = loadedFixture()
        search(fixture)

        fixture.searchField.text = "typed"
        fixture.searchField.background = ColorConstants.COLOR_SUCCESS

        pressEscape(fixture.searchField)

        assertRestored(fixture)
    }

    @Test
    fun `should restore the menu on Ctrl+Enter with an empty field`() {
        val fixture = loadedFixture()
        search(fixture)

        fixture.searchField.text = ""
        fixture.searchField.background = ColorConstants.COLOR_SUCCESS

        pressCtrlEnter(fixture.searchField)

        assertRestored(fixture)
    }

    @Test
    fun `should restore the menu from the clear button`() {
        val fixture = loadedFixture()
        search(fixture)

        fixture.searchField.text = "typed"
        fixture.searchField.background = ColorConstants.COLOR_SUCCESS

        clickClear(fixture)

        assertRestored(fixture)
    }

    @Test
    fun `should return a tagged nested entry to its group after reset`() {
        val fixture = loadedFixture()
        val leaf = fixture.leafItem

        clickTag(fixture, "tag (1)")

        // the search view shows the nested entry and the group keeps its other child
        assertThat(titles(fixture.contentMenu)).containsExactly("leaf")
        assertThat(fixture.contentMenu.getItem(0)).isSameAs(leaf)
        assertThat(titles(fixture.groupMenu)).containsExactly("other")

        clickClear(fixture)

        assertThat(titles(fixture.contentMenu)).containsExactly("group")
        assertThat(titles(fixture.groupMenu)).containsExactly("leaf", "other")
        assertThat(fixture.groupMenu.getItem(0)).isSameAs(leaf)
    }

    @Test
    fun `should keep every entry after two searches and one reset`() {
        val fixture = loadedFixture()

        clickTag(fixture, "tag (1)")
        clickTag(fixture, "tag (1)")

        clickClear(fixture)

        assertThat(titles(fixture.contentMenu)).containsExactly("group")
        assertThat(titles(fixture.groupMenu)).containsExactly("leaf", "other")
    }

    private fun search(fixture: Fixture) {
        clickTag(fixture, "tag (1)")
        assertThat(titles(fixture.contentMenu)).containsExactly("leaf")
    }

    private fun assertRestored(fixture: Fixture) {
        assertThat(titles(fixture.contentMenu)).containsExactly("group")
        assertThat(titles(fixture.groupMenu)).containsExactly("leaf", "other")
        assertThat(fixture.searchField.text).isEmpty()
        assertThat(fixture.searchField.background).isEqualTo(Color.WHITE)
    }

    private fun pressEscape(field: JTextField) {
        pressKey(
            field,
            KeyEvent(
                field,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_ESCAPE,
                KeyEvent.CHAR_UNDEFINED,
            )
        )
    }

    private fun pressCtrlEnter(field: JTextField) {
        pressKey(
            field,
            KeyEvent(
                field,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                InputEvent.CTRL_DOWN_MASK,
                KeyEvent.VK_ENTER,
                KeyEvent.CHAR_UNDEFINED,
            )
        )
    }

    private fun pressKey(field: JTextField, event: KeyEvent) {
        // the same handler the field runs for a real key press, invoked on the EDT
        SwingUtilities.invokeAndWait { field.keyListeners.forEach { it.keyPressed(event) } }
        flushEdt()
    }

    private fun clickClear(fixture: Fixture) {
        val button = findButton(fixture.panel.search.component)
        SwingUtilities.invokeAndWait {
            button.mouseListeners.forEach { listener ->
                listener.mouseReleased(
                    MouseEvent(button, MouseEvent.MOUSE_RELEASED, 0, 0, 0, 0, 1, false)
                )
            }
        }
        flushEdt()
    }

    private fun clickTag(fixture: Fixture, text: String) {
        val tagItem = (0 until fixture.tagsMenu.itemCount)
            .mapNotNull { fixture.tagsMenu.getItem(it) }
            .first { it.text == text }
        SwingUtilities.invokeAndWait { tagItem.doClick(0) }
        flushEdt()
    }

    private fun flushEdt() {
        SwingUtilities.invokeAndWait { }
    }

    private fun findButton(component: Component): JButton {
        if (component is JButton) {
            return component
        }
        if (component is Container) {
            component.components.forEach { child ->
                val found = findButtonOrNull(child)
                if (found != null) {
                    return found
                }
            }
        }
        error("no button in the search panel")
    }

    private fun findButtonOrNull(component: Component): JButton? {
        if (component is JButton) {
            return component
        }
        if (component is Container) {
            component.components.forEach { child ->
                findButtonOrNull(child)?.let { return it }
            }
        }
        return null
    }

    private fun titles(menu: JMenu): List<String> =
        (0 until menu.itemCount).map { menu.getItem(it).text }

    private fun loadedFixture(): Fixture {
        val latch = contentSwapLatch()
        val tags = listOf(TagAndKeys(name = "tag", keys = setOf(LEAF_ID)))
        val settingsService = settingsService(
            actions = listOf(
                group(GROUP_ID, leaf(LEAF_ID), leaf(OTHER_ID)),
            )
        )
        val tagsService = mockk<TagsService>(relaxed = true)
        every { tagsService.tagsTree() } returns emptyList()
        every { tagsService.tagAndKeys() } returns tags

        val service = uiService(
            settingsService = settingsService,
            tagsService = tagsService,
            tagsEnabled = true,
        )
        val panel = service.createMenu(JDialog())
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()

        val contentMenu = panel.menu.getMenu(0)
        val tagsMenu = panel.menu.getMenu(1)
        val groupMenu = contentMenu.getItem(0) as JMenu

        return Fixture(
            panel = panel,
            contentMenu = contentMenu,
            tagsMenu = tagsMenu,
            groupMenu = groupMenu,
            leafItem = groupMenu.getItem(0) as JMenuItem,
            searchField = panel.search.searchTextField,
        )
    }

    private fun contentSwapLatch(): CountDownLatch {
        val latch = CountDownLatch(1)
        val subscriber = object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                if (event is MenuContentReplacedDomainEvent) {
                    latch.countDown()
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(MenuContentReplacedDomainEvent::class.java)
        }
        DomainEventFactory.subscriberRegistrar.subscribe(subscriber)
        subscribers.add(subscriber)
        return latch
    }

    private fun uiService(
        settingsService: RocketActionSettingsService,
        tagsService: TagsService,
        tagsEnabled: Boolean,
    ): UiQuickActionService {
        val properties = mockk<GeneralPropertiesRepository>()
        every { properties.asBoolean(UsedPropertiesName.IS_ENABLE_TAGS, any()) } returns tagsEnabled

        val configurationApplication = mockk<ConfigurationApplication>()
        every { configurationApplication.all() } returns ApplicationConfigurations(
            variablesKey = "key",
            variablesManagers = emptyList(),
            numberButtonsOnChainActionSelectionPanel = 1,
            globalHotKeys = null,
        )

        val chainBasePanelFactory = mockk<ChainBasePanelFactory>()
        val chainBasePanel = mockk<ChainBasePanel>(relaxed = true)
        every { chainBasePanel.openAvailableActionsButton() } returns JButton()
        every { chainBasePanel.actionExecuteStatusPanel() } returns
            ActionExecuteStatusPanel(mockk<ActionExecutorService>(relaxed = true))
        every { chainBasePanelFactory.chainBasePanel } returns chainBasePanel

        val iconService = mockk<IconService>(relaxed = true)
        every { iconService.by(AppIcon.ROCKET_APP) } returns mockk<Icon>()
        val rocketActionContext = mockk<RocketActionContext>(relaxed = true)
        every { rocketActionContext.icon() } returns iconService
        val rocketActionContextFactory = mockk<RocketActionContextFactory>(relaxed = true)
        every { rocketActionContextFactory.context } returns rocketActionContext

        return UiQuickActionService(
            rocketActionSettingsService = settingsService,
            tagsService = tagsService,
            rocketActionContextFactory = rocketActionContextFactory,
            searchService = mockk<SearchService>(relaxed = true),
            configurationFrameFactory = mockk<ConfigurationFrameFactory>(relaxed = true),
            generalPropertiesRepository = properties,
            searchTextTransformer = mockk<SearchTextTransformer>(relaxed = true),
            configurationApplication = configurationApplication,
            chainBasePanelFactory = chainBasePanelFactory,
            variablesApplication = mockk<VariablesApplication>(relaxed = true),
        )
    }

    private fun settingsService(actions: List<RocketActionSettingsModel>): RocketActionSettingsService {
        val context = mockk<RocketActionContext>(relaxed = true)
        val groupPlugin = GroupRocketActionUi().apply { factory(context) }
        val leafPlugin = leafPlugin()

        val pluginService = mockk<RocketActionPluginApplicationService>()
        every { pluginService.by(any()) } answers {
            when (firstArg<String>()) {
                GroupRocketActionUi.TYPE -> groupPlugin
                LEAF_TYPE -> leafPlugin
                else -> null
            }
        }

        val repository = mockk<RocketActionSettingsRepository>()
        every { repository.actions() } returns ActionsModel(actions = actions)

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
                    val item = JMenuItem(settings.id())
                    return object : RocketAction {
                        override fun contains(search: String): Boolean = settings.id().contains(search)
                        override fun isChanged(actionSettings: RocketActionSettings): Boolean = false
                        override fun component(): Component = item
                    }
                }

                override fun type(): RocketActionType = RocketActionType { LEAF_TYPE }
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
        type = LEAF_TYPE,
        settings = emptyList(),
        actions = emptyList(),
        tags = emptyList(),
    )

    private data class Fixture(
        val panel: ManuAndSearchPanel,
        val contentMenu: JMenu,
        val tagsMenu: JMenu,
        val groupMenu: JMenu,
        val leafItem: JMenuItem,
        val searchField: JTextField,
    )

    private companion object {
        const val GROUP_ID = "group"
        const val LEAF_ID = "leaf"
        const val OTHER_ID = "other"
        const val LEAF_TYPE = "TEST"
    }
}
