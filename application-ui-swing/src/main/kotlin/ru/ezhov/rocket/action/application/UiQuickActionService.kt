package ru.ezhov.rocket.action.application

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.ConfigurationFrame
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.handlers.server.Server
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.search.application.SearchInstance
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.tags.domain.TagNode
import ru.ezhov.rocket.action.ui.utils.swing.common.MoveUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Component
import java.awt.Desktop
import java.awt.FlowLayout
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextArea
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.SwingWorker
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

class UiQuickActionService(
    private val rocketActionSettingsRepository: RocketActionSettingsRepository,
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val tagsService: TagsService,
) {
    private var configurationFrame: ConfigurationFrame? = null
    private var baseDialog: JDialog? = null

    fun createMenu(baseDialog: JDialog): JMenuBar {
        this.baseDialog = baseDialog
        return try {
            val menuBar = JMenuBar()
            val menu = JMenu()
            menuBar.add(menu)
            //TODO: favorites
            //menuBar.add(createFavoriteComponent());

            menuBar.add(createTagsMenu(menu));

            menuBar.add(createSearchField(menu))
            val moveLabel = JLabel(RocketActionContextFactory.context.icon().by(AppIcon.MOVE))
            MoveUtil.addMoveAction(movableComponent = baseDialog, grabbedComponent = moveLabel)

            val editorLabel = JLabel(RocketActionContextFactory.context.icon().by(AppIcon.PENCIL))
            val actionOnEditor = createEditorActionListener()
            editorLabel.addMouseListener(object : MouseAdapter() {
                override fun mouseReleased(e: MouseEvent?) {
                    actionOnEditor.actionPerformed(null)
                }
            })
            menuBar.add(editorLabel)

            menuBar.add(moveLabel)
            CreateMenuWorker(menu).execute()
            menuBar
        } catch (e: Exception) {
            throw UiQuickActionServiceException("Error", e)
        }
    }

    private fun rocketActionSettings(): ActionsModel = rocketActionSettingsRepository.actions()

    private fun createSearchField(menu: JMenu): Component =
        JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            border = BorderFactory.createEmptyBorder()
            val textField =
                TextFieldWithText("Search")
                    .apply { ->
                        val tf = this
                        columns = 5
                        addKeyListener(object : KeyAdapter() {
                            override fun keyPressed(e: KeyEvent) {
                                if (e.keyCode == KeyEvent.VK_ENTER) {
                                    val cache = RocketActionComponentCacheFactory.cache
                                    if (text.isNotEmpty()) {

                                        val idsRA = SearchInstance.service().search(text).toSet()
                                        val raByFullText = cache.byIds(idsRA)
                                        val raByContains = cache
                                            .all()
                                            .filter { it.contains(text) }

                                        (raByFullText + raByContains)
                                            .toSet()
                                            .takeIf { it.isNotEmpty() }
                                            ?.let { ra ->
                                                logger.info { "Found by search '$text': ${ra.size}" }
                                                tf.background = Color.GREEN
                                                fillMenuByRocketAction(ra, menu)
                                            }
                                    } else {
                                        SwingUtilities.invokeLater { tf.background = Color.WHITE }
                                        CreateMenuWorker(menu).execute()
                                    }
                                } else if (e.keyCode == KeyEvent.VK_ESCAPE) {
                                    resetSearch(textField = tf, menu = menu)
                                }
                            }
                        })
                    }
            add(textField)
            add(
                JLabel(RocketActionContextFactory.context.icon().by(AppIcon.CLEAR))
                    .apply {
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
                    menu.add(createTools(baseDialog!!))
                    menu.doClick()
                }
            }
    }

    private fun createTagsMenu(baseMenu: JMenu): JMenu {
        // TODO ezhov move to storage
        val iconTree = ImageIcon(this::class.java.getResource("/icons/tree_16x16.png"))
        val iconTag = ImageIcon(this::class.java.getResource("/icons/tag_16x16.png"))

        val menu = JMenu().apply { icon = iconTag }
        val menuTree = JMenu("Tags tree").apply { icon = iconTree }

        val invokeSearch: (keys: Set<String>) -> Unit = { keys ->
            val cache = RocketActionComponentCacheFactory.cache
            val actions = cache.byIds(keys).toSet()

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

        menu.add(menuTree)

        tagsService.tagAndKeys().forEach { t ->
            menu.add(
                JMenuItem("${t.name} (${t.keys.size})")
                    .apply {
                        icon = iconTag
                        addActionListener {
                            invokeSearch(t.keys)
                        }
                    }
            )
        }

        return menu
    }

    private fun resetSearch(textField: JTextField, menu: JMenu) {
        textField.text = ""
        SwingUtilities.invokeLater { textField.background = Color.WHITE }
        CreateMenuWorker(menu).execute()
    }

    private fun createTools(baseDialog: JDialog): JMenu {
        val menuTools = JMenu("Tools")
        menuTools.icon = RocketActionContextFactory.context.icon().by(AppIcon.WRENCH)

        val menuItemUpdate = JMenuItem("Refresh")
        menuItemUpdate.icon = RocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
        menuItemUpdate.addActionListener(updateListener())
        menuTools.add(menuItemUpdate)

        val menuItemEditor = JMenuItem("Editor")
        menuItemEditor.icon = RocketActionContextFactory.context.icon().by(AppIcon.PENCIL)
        menuItemEditor.addActionListener(createEditorActionListener())
        menuTools.add(menuItemEditor)

        val menuInfo = JMenu("Information")
        menuInfo.icon = RocketActionContextFactory.context.icon().by(AppIcon.INFO)
        val notFound = { key: String -> "Information on field '$key' not found" }
        menuInfo.add(
            JMenuItem(
                generalPropertiesRepository
                    .asString(UsedPropertiesName.VERSION, notFound("version"))
            )
        )
        menuInfo.add(
            JMenuItem(
                generalPropertiesRepository
                    .asString(UsedPropertiesName.INFO, notFound("information"))
            )
        )
        menuInfo.add(
            JMenuItem(
                generalPropertiesRepository
                    .asString(UsedPropertiesName.REPOSITORY, notFound("link to repository"))
            )
                .apply {
                    addActionListener {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI.create(text))
                        }
                    }
                }
        )

        menuInfo.add(JSeparator())

        menuInfo.add(
            JMenuItem("Available Handlers")
                .apply {
                    addActionListener {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI.create("http://localhost:${Server.port()}"))
                        }
                    }
                }
        )

        menuInfo.add(JSeparator())
        menuInfo.add(createPropertyMenu())
        menuTools.add(menuInfo)

        menuTools.add(JMenuItem("Exit").apply {
            icon = RocketActionContextFactory.context.icon().by(AppIcon.X)
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
                if (configurationFrame == null) {
                    try {
                        configurationFrame = ConfigurationFrame(
                            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
                            rocketActionSettingsRepository = rocketActionSettingsRepository,
                            updateActionListener = updateListener()
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        RocketActionContextFactory.context.notification()
                            .show(NotificationType.ERROR, "Error creating configuration menu")
                    }
                }
                if (configurationFrame != null) {
                    configurationFrame!!.setVisible(true)
                }
            }
        }

    private fun updateListener() =
        ActionListener {
            SwingUtilities.invokeLater {
                var newMenuBar: JMenuBar? = null
                try {
                    newMenuBar = createMenu(baseDialog!!)
                } catch (ex: UiQuickActionServiceException) {
                    ex.printStackTrace()
                    RocketActionContextFactory.context.notification()
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

    private fun createPropertyMenu(): JMenu =
        JMenu("Available properties from the command line").apply {
            UsedPropertiesName.values()
                .forEach { pn ->
                    this.add(
                        JTextArea(
                            "${pn.propertyName}\n${pn.description}"
                        ).apply { isEditable = false }
                    )
                }
        }

    private inner class CreateMenuWorker(private val menu: JMenu) : SwingWorker<List<Component>, String?>() {
        init {
            menu.icon = RocketActionContextFactory.context.icon().by(AppIcon.LOADER)
            menu.removeAll()
        }

        override fun doInBackground(): List<Component> {
            val actionSettings = rocketActionSettings()
            fillCache(actionSettings.actions)
            val cache = RocketActionComponentCacheFactory.cache
            val components = mutableListOf<Component>()
            for (rocketActionSettings in actionSettings.actions) {
                rocketActionPluginApplicationService.by(rocketActionSettings.type)
                    ?.factory(RocketActionContextFactory.context)
                    ?.let {
                        (
                            cache
                                .by(rocketActionSettings.id)
                                ?.let { action ->
                                    logger.debug {
                                        "found in cache type='${rocketActionSettings.type}'" +
                                            "id='${rocketActionSettings.id}"
                                    }

                                    action.component()
                                }
                                ?: run {
                                    logger.debug {
                                        "not found in cache type='${rocketActionSettings.type}'" +
                                            "id='${rocketActionSettings.id}. Create component"
                                    }

                                    it.create(
                                        settings = rocketActionSettings.to(),
                                        context = RocketActionContextFactory.context
                                    )?.component()
                                }
                            )
                            ?.let { component ->
                                components.add(component)
                            }
                    }
            }
            components.add(createTools(baseDialog!!))
            return components.toList()
        }

        private fun fillCache(actionSettings: List<RocketActionSettingsModel>) {
            RocketActionComponentCacheFactory
                .cache
                .let { cache ->
                    for (rocketActionSettings in actionSettings) {
                        val rau = rocketActionPluginApplicationService.by(rocketActionSettings.type)
                            ?.factory(RocketActionContextFactory.context)
                        if (rau != null) {
                            if (rocketActionSettings.type != GroupRocketActionUi.TYPE) {
                                val mustBeCreate = cache
                                    .by(rocketActionSettings.id)
                                    ?.isChanged(rocketActionSettings.to()) ?: true

                                logger.debug {
                                    "must be create '$mustBeCreate' type='${rocketActionSettings.type}'" +
                                        "id='${rocketActionSettings.id}'"
                                }

                                if (mustBeCreate) {
                                    rau.create(
                                        settings = rocketActionSettings.to(),
                                        context = RocketActionContextFactory.context
                                    )
                                        ?.let { action ->
                                            logger.debug {
                                                "added to cache type='${rocketActionSettings.type}'" +
                                                    "id='${rocketActionSettings.id}'"
                                            }

                                            cache.add(
                                                rocketActionSettings.id,
                                                action
                                            )
                                        }
                                }
                            } else {
                                if (rocketActionSettings.actions.isNotEmpty()) {
                                    fillCache(rocketActionSettings.actions)
                                }
                            }
                        }
                    }
                }
        }

        override fun done() {
            try {
                val components = this.get()
                components.forEach { menu.add(it) }
                menu.icon = RocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP)
            } catch (ex: Exception) {
                logger.error(ex) { "Error when load app" }
            }
        }
    }
}
