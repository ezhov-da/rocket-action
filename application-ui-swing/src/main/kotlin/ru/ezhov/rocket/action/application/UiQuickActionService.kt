package ru.ezhov.rocket.action.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.ConfigurationFrameFactory
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.event.RocketActionSettingsCreatedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiListener
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.eventui.model.RefreshUiEvent
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.search.application.SearchService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.tags.domain.TagNode
import ru.ezhov.rocket.action.application.ui.color.ColorConstants
import ru.ezhov.rocket.action.ui.utils.swing.common.MoveUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

@Service
class UiQuickActionService(
    private val rocketActionSettingsService: RocketActionSettingsService,
    private val tagsService: TagsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val searchService: SearchService,
    private val configurationFrameFactory: ConfigurationFrameFactory
) {
    private var baseDialog: JDialog? = null

    init {
        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                SwingUtilities.invokeLater { updateMenu() }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(RocketActionSettingsCreatedDomainEvent::class.java)
        })

        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is RefreshUiEvent) {
                    SwingUtilities.invokeLater { updateMenu() }
                }
            }
        })
    }

    fun createMenu(baseDialog: JDialog): JMenuBar {
        this.baseDialog = baseDialog
        return try {
            val menuBar = JMenuBar()
            val menu = JMenu()
            menuBar.add(menu)
            //TODO: favorites
            //menuBar.add(createFavoriteComponent());

            val tagsMenu = createEmptyTagsMenu()
            menuBar.add(tagsMenu);
            menuBar.add(createSearchField(menu))

            val moveLabel = JLabel(rocketActionContextFactory.context.icon().by(AppIcon.MOVE))
            MoveUtil.addMoveAction(movableComponent = baseDialog, grabbedComponent = moveLabel)

            menuBar.add(createTools(baseDialog))

            menuBar.add(moveLabel)
            CreateMenuOrGetExistsWorker(menu, Action.Create(tagsMenu)).execute()
            menuBar
        } catch (e: Exception) {
            throw UiQuickActionServiceException("Error", e)
        }
    }

    private fun createSearchField(menu: JMenu): Component =
        JPanel(FlowLayout(FlowLayout.LEFT, 2, 1)).apply {
            border = BorderFactory.createEmptyBorder()
            val textField =
                TextFieldWithText("Search")
                    .apply { ->
                        val tf = this
                        columns = 5
                        addKeyListener(object : KeyAdapter() {
                            override fun keyPressed(e: KeyEvent) {
                                if (e.keyCode == KeyEvent.VK_ENTER) {
                                    if (text.isNotEmpty()) {

                                        val idsRA = searchService.search(text).toSet()
                                        val raByFullText = rocketActionSettingsService.actionsByIds(idsRA)
                                        val raByContains = rocketActionSettingsService.actionsByContains(text)

                                        (raByFullText + raByContains)
                                            .toSet()
                                            .takeIf { it.isNotEmpty() }
                                            ?.let { ra ->
                                                logger.info { "Found by search '$text': ${ra.size}" }
                                                tf.background = ColorConstants.COLOR_SUCCESS
                                                fillMenuByRocketAction(ra, menu)
                                            }
                                    } else {
                                        SwingUtilities.invokeLater { tf.background = Color.WHITE }
                                        CreateMenuOrGetExistsWorker(menu, Action.Restore).execute()
                                    }
                                } else if (e.keyCode == KeyEvent.VK_ESCAPE) {
                                    resetSearch(textField = tf, menu = menu)
                                }
                            }
                        })
                    }
            add(textField)

            val backgroundColor = JMenu().background
            background = backgroundColor
            add(
                JButton(rocketActionContextFactory.context.icon().by(AppIcon.CLEAR))
                    .apply {
                        toolTipText = "Clear search"
                        background = backgroundColor
                        isBorderPainted = false
                        addMouseListener(object : MouseAdapter() {
                            override fun mouseReleased(e: MouseEvent?) {
                                resetSearch(textField = textField, menu = menu)
                            }
                        })
                    }
            )
        }

    private fun fillMenuByRocketAction(rocketActions: Set<RocketAction>, menu: JMenu) {
        rocketActions.takeIf { it.isNotEmpty() }
            ?.let { ccl ->
                SwingUtilities.invokeLater {
                    menu.removeAll()
                    ccl.forEach { menu.add(it.component()) }
                    menu.doClick()
                }
            }
    }

    private fun createEmptyTagsMenu(): JMenu {
        val iconTag = ImageIcon(this::class.java.getResource("/icons/tag_16x16.png"))
        return JMenu().apply { icon = iconTag }
    }

    private fun fillTagsMenu(baseMenu: JMenu, tagsMenu: JMenu) {
        // TODO ezhov move to storage
        val iconTree = ImageIcon(this::class.java.getResource("/icons/tree_16x16.png"))
        val iconTag = ImageIcon(this::class.java.getResource("/icons/tag_16x16.png"))

        val menuTree = JMenu("Tags tree").apply { icon = iconTree }

        val invokeSearch: (keys: Set<String>) -> Unit = { keys ->
            val actions = rocketActionSettingsService.actionsByIds(keys).toSet()

            fillMenuByRocketAction(rocketActions = actions, menu = baseMenu)
        }

        fun recursive(parent: JMenu, child: TagNode) {
            if (child.children.isEmpty()) {
                parent.add(JMenuItem("${child.name} (${child.keys.size})")
                    .apply {
                        icon = iconTag
                        addActionListener {
                            invokeSearch(child.keys.toSet())
                        }
                    }
                )
            } else {
                parent.add(
                    JMenuItem("${child.name} (${child.keys.size})")
                        .apply {
                            icon = iconTag
                            addActionListener {
                                invokeSearch(child.keys.toSet())
                            }
                        })
                val parentInner = JMenu(child.name).apply {
                    icon = iconTree
                }
                parent.add(parentInner)
                child.children.forEach { ch ->
                    recursive(parentInner, ch)
                }
            }
        }

        tagsService.tagsTree().forEach { node ->
            recursive(menuTree, node)
        }

        tagsMenu.add(menuTree)

        tagsService.tagAndKeys().forEach { t ->
            tagsMenu.add(
                JMenuItem("${t.name} (${t.keys.size})")
                    .apply {
                        icon = iconTag
                        addActionListener {
                            invokeSearch(t.keys)
                        }
                    }
            )
        }
    }

    private fun resetSearch(textField: JTextField, menu: JMenu) {
        textField.text = ""
        SwingUtilities.invokeLater { textField.background = Color.WHITE }
        CreateMenuOrGetExistsWorker(menu, Action.Restore).execute()
    }

    private fun createTools(baseDialog: JDialog): JMenu {
        val menuTools = JMenu()
        menuTools.icon = rocketActionContextFactory.context.icon().by(AppIcon.WRENCH)

        val menuItemUpdate = JMenuItem("Refresh")
        menuItemUpdate.icon = rocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
        menuItemUpdate.addActionListener(updateListener())
        menuTools.add(menuItemUpdate)

        val menuItemEditor = JMenuItem("Editor")
        menuItemEditor.icon = rocketActionContextFactory.context.icon().by(AppIcon.PENCIL)
        menuItemEditor.addActionListener(createEditorActionListener())
        menuTools.add(menuItemEditor)

        menuTools.add(JMenuItem("Exit").apply {
            icon = rocketActionContextFactory.context.icon().by(AppIcon.X)
            addActionListener {
                SwingUtilities.invokeLater {
                    baseDialog.dispose()
                    exitProcess(0)
                }
            }
        })
        return menuTools
    }

    private fun createEditorActionListener(): ActionListener =
        ActionListener {
            SwingUtilities.invokeLater {
                configurationFrameFactory.configurationFrame.show()
            }
        }

    private fun updateListener() =
        ActionListener { updateMenu() }

    private fun updateMenu() {
        SwingUtilities.invokeLater {
            var newMenuBar: JMenuBar? = null
            try {
                newMenuBar = createMenu(baseDialog!!)
            } catch (ex: UiQuickActionServiceException) {
                ex.printStackTrace()
                rocketActionContextFactory.context.notification()
                    .show(NotificationType.ERROR, "Error creating tool menu")
            }
            if (newMenuBar != null) {
                // while a crutch, but we know that "yet" :)
                baseDialog!!.jMenuBar.removeAll()
                baseDialog!!.jMenuBar = newMenuBar
                baseDialog!!.revalidate()
                baseDialog!!.repaint()
            }
        }
    }

    private inner class CreateMenuOrGetExistsWorker(
        private val baseMenu: JMenu,
        private val action: Action
    ) :
        SwingWorker<List<Component>, String?>() {

        init {
            baseMenu.icon = rocketActionContextFactory.context.icon().by(AppIcon.LOADER)
            baseMenu.removeAll()
        }

        override fun doInBackground(): List<Component> = when (action) {
            is Action.Create -> {
                val components = rocketActionSettingsService.loadAndGetAllComponents()
                fillTagsMenu(baseMenu, action.tagsMenu)
                DomainEventFactory.publisher.publish(listOf(CreateMenuDomainEvent()))
                components
            }

            is Action.Restore -> {
                DomainEventFactory.publisher.publish(listOf(RestoreMenuDomainEvent()))
                rocketActionSettingsService.getAllExistsComponents()
            }
        }

        override fun done() {
            try {
                val components = this.get()
                components.forEach { baseMenu.add(it) }
                baseMenu.icon = rocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP)
            } catch (ex: Exception) {
                logger.error(ex) { "Error when load app" }
            }
        }
    }

    sealed class Action {
        class Create(val tagsMenu: JMenu) : Action()
        object Restore : Action()
    }
}
