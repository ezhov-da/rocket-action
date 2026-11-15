package ru.ezhov.rocket.action.application

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
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
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.search.application.SearchService
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.SwingUtilities

internal class UiQuickActionServiceReloadTest {
    private val subscribers = mutableListOf<DomainEventSubscriber>()
    private val observedContent = mutableListOf<String>()
    private val contentLatches = java.util.concurrent.ConcurrentHashMap<Int, CountDownLatch>()
    private lateinit var contentMenu: JMenu
    private val rocketIcon = mockk<Icon>()
    private val loaderIcon = mockk<Icon>()

    init {
        val subscriber = object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                if (event is MenuContentReplacedDomainEvent) {
                    val text = contentMenu.getItem(0)?.text ?: ""
                    val count = synchronized(observedContent) {
                        observedContent.add(text)
                        observedContent.size
                    }
                    contentLatches[count]?.countDown()
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(MenuContentReplacedDomainEvent::class.java)
        }
        subscribers.add(subscriber)
        DomainEventFactory.subscriberRegistrar.subscribe(subscriber)
    }

    /**
     * Registers a latch that is released when the [number]-th content swap happens.
     * Register it before the reload starts.
     */
    private fun contentLatch(number: Int): CountDownLatch =
        CountDownLatch(1).also { contentLatches[number] = it }

    @AfterEach
    fun unsubscribe() {
        subscribers.forEach { DomainEventFactory.subscriberRegistrar.unsubscribe(it) }
        subscribers.clear()
    }

    @Test
    fun `should keep the chrome and swap the content only when it is ready`() {
        val settingsService = mockk<RocketActionSettingsService>()
        every { settingsService.newReloadScope() } answers { ActionReloadScope() }

        val firstLoadStarted = CountDownLatch(1)
        val firstLoadAllowed = CountDownLatch(1)
        val secondLoadStarted = CountDownLatch(1)
        val secondLoadAllowed = CountDownLatch(1)
        val loadCount = AtomicInteger()
        var itemCountDuringSecondLoad = -1

        every { settingsService.loadAndGetAllComponents(any()) } answers {
            when (loadCount.incrementAndGet()) {
                1 -> {
                    firstLoadStarted.countDown()
                    firstLoadAllowed.await(5, TimeUnit.SECONDS)
                }

                2 -> {
                    itemCountDuringSecondLoad = contentMenu.itemCount
                    secondLoadStarted.countDown()
                    secondLoadAllowed.await(5, TimeUnit.SECONDS)
                }
            }
            listOf(JMenuItem("content-${loadCount.get()}"))
        }

        val first = contentLatch(1)
        val second = contentLatch(2)
        val service = service(settingsService = settingsService, tagsEnabled = false)
        val dialog = JDialog()

        val panel = service.createMenu(dialog)
        contentMenu = panel.menu.getMenu(0)
        val bar = panel.menu
        val toolsMenu = findToolsMenu(bar)

        assertThat(firstLoadStarted.await(5, TimeUnit.SECONDS)).isTrue()
        firstLoadAllowed.countDown()
        assertThat(first.await(5, TimeUnit.SECONDS)).isTrue()

        // the content swap comes before the content-replaced event
        assertThat(contentMenu.itemCount).isEqualTo(1)
        assertThat(contentMenu.getItem(0).text).isEqualTo("content-1")
        assertThat(observedContent).containsExactly("content-1")

        // the user types into the search field and refreshes
        panel.search.searchTextField.text = "typed"
        SwingUtilities.invokeAndWait { findRefreshItem(bar).doClick() }

        assertThat(secondLoadStarted.await(5, TimeUnit.SECONDS)).isTrue()
        secondLoadAllowed.countDown()
        assertThat(second.await(5, TimeUnit.SECONDS)).isTrue()

        // the bar, the search field and the tools menu are the same instances
        assertThat(panel.menu).isSameAs(bar)
        assertThat(panel.search.searchTextField.text).isEqualTo("typed")
        assertThat(findToolsMenu(bar)).isSameAs(toolsMenu)
        assertThat(contentMenu.getItem(0).text).isEqualTo("content-2")
        assertThat(observedContent).containsExactly("content-1", "content-2")

        // the previous entries stayed visible for the whole reload
        assertThat(itemCountDuringSecondLoad).isEqualTo(1)

        // tags are disabled: the bar has only the content and tools menus
        assertThat(countMenus(bar)).isEqualTo(2)
    }

    @Test
    fun `should keep one tags menu instance when tags are enabled`() {
        val settingsService = mockk<RocketActionSettingsService>()
        every { settingsService.newReloadScope() } answers { ActionReloadScope() }

        val loadCount = AtomicInteger()
        every { settingsService.loadAndGetAllComponents(any()) } answers {
            // the latches are driven by MenuContentReplacedDomainEvent, i.e. after the content swap
            loadCount.incrementAndGet()
            listOf(JMenuItem("content-${loadCount.get()}"))
        }

        val first = contentLatch(1)
        val second = contentLatch(2)
        val service = service(settingsService = settingsService, tagsEnabled = true)
        val dialog = JDialog()

        val panel = service.createMenu(dialog)
        contentMenu = panel.menu.getMenu(0)
        val bar = panel.menu
        val tagsMenu = bar.getMenu(1)

        assertThat(first.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(tagsMenu.itemCount).isEqualTo(1)

        SwingUtilities.invokeAndWait { findRefreshItem(bar).doClick() }
        assertThat(second.await(5, TimeUnit.SECONDS)).isTrue()

        assertThat(bar.getMenu(1)).isSameAs(tagsMenu)
        assertThat(tagsMenu.itemCount).isEqualTo(1)
    }

    @Test
    fun `should keep the typed text and a non empty menu across three consecutive refreshes`() {
        val settingsService = mockk<RocketActionSettingsService>()
        every { settingsService.newReloadScope() } answers { ActionReloadScope() }

        val loadCount = AtomicInteger()
        val itemCountsDuringLoad = mutableListOf<Int>()
        every { settingsService.loadAndGetAllComponents(any()) } answers {
            val number = loadCount.incrementAndGet()
            if (number > 1) {
                // what the user sees while the reload is still running
                synchronized(itemCountsDuringLoad) { itemCountsDuringLoad.add(contentMenu.itemCount) }
            }
            listOf(JMenuItem("content-$number"))
        }

        val latches = (1..3).associateWith { contentLatch(it) }
        val service = service(settingsService = settingsService, tagsEnabled = false)
        val panel = service.createMenu(JDialog())
        contentMenu = panel.menu.getMenu(0)
        val bar = panel.menu
        val searchField = panel.search.searchTextField

        assertThat(latches.getValue(1).await(5, TimeUnit.SECONDS)).isTrue()

        searchField.text = "typed"
        SwingUtilities.invokeAndWait { findRefreshItem(bar).doClick() }
        assertThat(latches.getValue(2).await(5, TimeUnit.SECONDS)).isTrue()

        searchField.text = "typed again"
        SwingUtilities.invokeAndWait { findRefreshItem(bar).doClick() }
        assertThat(latches.getValue(3).await(5, TimeUnit.SECONDS)).isTrue()

        // the menu never became empty while reloading
        assertThat(itemCountsDuringLoad).containsOnly(1)

        // the last reload is displayed and the chrome (with the hot key registration) is untouched
        assertThat(contentMenu.itemCount).isEqualTo(1)
        assertThat(contentMenu.getItem(0).text).isEqualTo("content-3")
        assertThat(panel.search.searchTextField).isSameAs(searchField)
        assertThat(searchField.text).isEqualTo("typed again")
        assertThat(panel.menu).isSameAs(bar)

        // the main menu keeps the application icon and never shows the loader icon
        assertThat(contentMenu.icon).isSameAs(rocketIcon)
        assertThat(contentMenu.icon).isNotSameAs(loaderIcon)
    }

    private fun service(
        settingsService: RocketActionSettingsService,
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

        val tagsService = mockk<TagsService>(relaxed = true)
        every { tagsService.tagsTree() } returns emptyList()
        every { tagsService.tagAndKeys() } returns emptyList()

        val chainBasePanelFactory = mockk<ChainBasePanelFactory>()
        val chainBasePanel = mockk<ChainBasePanel>(relaxed = true)
        every { chainBasePanel.openAvailableActionsButton() } returns JButton()
        every { chainBasePanel.actionExecuteStatusPanel() } returns
            ActionExecuteStatusPanel(mockk<ActionExecutorService>(relaxed = true))
        every { chainBasePanelFactory.chainBasePanel } returns chainBasePanel

        val iconService = mockk<IconService>(relaxed = true)
        every { iconService.by(AppIcon.ROCKET_APP) } returns rocketIcon
        every { iconService.by(AppIcon.LOADER) } returns loaderIcon
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

    private fun findToolsMenu(bar: JMenuBar): JMenu =
        (0 until bar.componentCount)
            .map { bar.getComponent(it) }
            .filterIsInstance<JMenu>()
            .first { menu -> (0 until menu.itemCount).any { menu.getItem(it)?.text == "Refresh" } }

    private fun findRefreshItem(bar: JMenuBar): JMenuItem {
        val tools = findToolsMenu(bar)
        return (0 until tools.itemCount)
            .map { tools.getItem(it) }
            .first { it?.text == "Refresh" }!!
    }

    private fun countMenus(bar: JMenuBar): Int =
        (0 until bar.componentCount).count { bar.getComponent(it) is JMenu }
}
