package ru.ezhov.rocket.action.application.configuration.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiListener
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.configuration.ui.event.model.RemoveSettingUiEvent
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.ui.PluginManagerFrame
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.interfaces.ui.VariablesFrame
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.AbstractAction
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

private val logger = KotlinLogging.logger { }

class ConfigurationFrame(
    rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsService: RocketActionSettingsService,
    updateActionListener: ActionListener
) {
    private val frame: JFrame = JFrame()
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog
    private val updateActionListener: ActionListener
    private var finalTree: JTree? = null

    private fun panel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(tree(), BorderLayout.CENTER)
        return panel
    }

    private fun tree(): JPanel {
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.setDividerLocation(0.5)
        splitPane.resizeWeight = 0.5
        val actionsModel = rocketActionSettingsService.actionsModel()
        val root = DefaultMutableTreeNode(null, true)
        fillTreeNodes(actionsModel.actions, root)
        setTitle(actionsModel.lastChangedDate)
        val defaultTreeModel = DefaultTreeModel(root)
        val rocketActionSettingsPanel = EditorRocketActionSettingsPanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
        )
        val panel = JPanel(BorderLayout())
        val tree = JTree(defaultTreeModel)
        finalTree = tree
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.addTreeSelectionListener { e: TreeSelectionEvent ->
            val path = e.newLeadSelectionPath ?: return@addTreeSelectionListener
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val o = node.userObject
            if (o != null) {
                val settings = o as TreeRocketActionSettings
                SwingUtilities.invokeLater {
                    rocketActionSettingsPanel.show(
                        settings = settings,
                        callback = object : SavedRocketActionSettingsPanelCallback {
                            override fun saved(settings: TreeRocketActionSettings) {
                                node.userObject = settings
                                SwingUtilities.invokeLater {
                                    tree.repaint()
                                }
                            }
                        }
                    )
                }
            }
        }
        tree.cellRenderer = RocketActionSettingsCellRender()
        tree.isRootVisible = false

        val panelTree = JPanel(BorderLayout())
        val innerPanelTree = JPanel(BorderLayout())

        val toolbar = JToolBar(JToolBar.VERTICAL)
            .apply {
                isFloatable = false
                isRollover = false
                add(
                    JButton()
                        .apply {
                            val expandIcon = RocketActionContextFactory.context.icon().by(AppIcon.EXPAND)
                            val collapseIcon = RocketActionContextFactory.context.icon().by(AppIcon.COLLAPSE)
                            var isExpanded = false
                            val setButtonState: (Boolean) -> Unit = { b ->
                                val resultIcon = if (b) collapseIcon else expandIcon
                                SwingUtilities.invokeLater {
                                    this.toolTipText = if (b) "Collapse all" else "Expand all"
                                    this.icon = resultIcon
                                }
                            }
                            setButtonState(isExpanded)
                            addActionListener {
                                isExpanded = !isExpanded
                                JTreeUtil.setTreeExpandedState(tree, isExpanded)
                                setButtonState(isExpanded)
                            }
                        }
                )
            }

        innerPanelTree.add(
            SearchInTreePanel(root = root, treeModel = defaultTreeModel, tree = tree),
            BorderLayout.NORTH
        )
        innerPanelTree.add(JScrollPane(tree), BorderLayout.CENTER)

        panelTree.add(innerPanelTree, BorderLayout.CENTER)
        panelTree.add(toolbar, BorderLayout.WEST)

        val panelSaveTree = JPanel()
        val buttonSaveTree = JButton(
            "Save all configuration to storage",
            RocketActionContextFactory.context.icon().by(AppIcon.SAVE)
        )
        buttonSaveTree.addActionListener { saveSettings(defaultTreeModel) }
        panelSaveTree.add(buttonSaveTree)
        panelTree.add(panelSaveTree, BorderLayout.SOUTH)
        splitPane.leftComponent = panelTree
        splitPane.rightComponent = rocketActionSettingsPanel
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON_OR_INSERT
        tree.transferHandler = TreeTransferHandler()
        val expandedSet = mutableSetOf<TreePath>()
        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent?) {
                event?.let {
                    logger.debug { "Expanded tree path '${it.path.lastPathComponent}'" }
                    expandedSet.add(it.path)
                }
            }

            override fun treeCollapsed(event: TreeExpansionEvent?) {
                event?.let {
                    logger.debug { "Collapsed tree path '${it.path.lastPathComponent}'" }
                    expandedSet.remove(it.path)
                }
            }
        })
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val treePath = tree.getClosestPathForLocation(e.x, e.y) ?: return
                    val mutableTreeNode = treePath.lastPathComponent as DefaultMutableTreeNode
                    val userObject = mutableTreeNode.userObject as? TreeRocketActionSettings
                    val popupMenu = JPopupMenu()
                    popupMenu.add(JMenuItem(object : AbstractAction() {
                        override fun actionPerformed(e: ActionEvent) {
                            createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                override fun create(settings: TreeRocketActionSettings) {
                                    SwingUtilities.invokeLater {
                                        defaultTreeModel.insertNodeInto(
                                            DefaultMutableTreeNode(settings, true),
                                            mutableTreeNode.parent as MutableTreeNode,
                                            mutableTreeNode.parent.getIndex(mutableTreeNode)
                                        )
                                    }
                                }
                            })
                        }

                        init {
                            putValue(NAME, "Add above")
                            putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                        }
                    }))
                    popupMenu.add(JMenuItem(
                        object : AbstractAction() {
                            override fun actionPerformed(e: ActionEvent) {
                                createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                    override fun create(settings: TreeRocketActionSettings) {
                                        SwingUtilities.invokeLater {
                                            defaultTreeModel.insertNodeInto(
                                                DefaultMutableTreeNode(settings, true),
                                                mutableTreeNode.parent as MutableTreeNode,
                                                mutableTreeNode.parent.getIndex(mutableTreeNode) + 1
                                            )
                                        }
                                    }

                                })
                            }

                            init {
                                putValue(NAME, "Add below")
                                putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                            }
                        }
                    ))
                    if (userObject?.settings?.type == GroupRocketActionUi.TYPE) {
                        popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                        override fun create(settings: TreeRocketActionSettings) {
                                            SwingUtilities.invokeLater {
                                                mutableTreeNode.add(
                                                    DefaultMutableTreeNode(settings, true)
                                                )
                                                defaultTreeModel.reload(mutableTreeNode)
                                            }
                                        }
                                    })
                                }

                                init {
                                    putValue(NAME, "Create and add as child")
                                    putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                                }
                            }
                        ))
                    }
                    if (
                        userObject != null &&
                        userObject.settings.type != GroupRocketActionUi.TYPE &&
                        userObject.settings is MutableRocketActionSettings
                    ) {
                        popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    val settings = userObject.settings
                                    val duplicate = settings.copy(userObject.settings)
                                    SwingUtilities.invokeLater {
                                        defaultTreeModel.insertNodeInto(
                                            DefaultMutableTreeNode(
                                                TreeRocketActionSettings(
                                                    configuration = userObject.configuration,
                                                    settings = duplicate,
                                                ),
                                                true
                                            ),
                                            mutableTreeNode.parent as MutableTreeNode,
                                            mutableTreeNode.parent.getIndex(mutableTreeNode) + 1
                                        )
                                    }
                                }

                                init {
                                    putValue(NAME, "Duplicate")
                                    putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.FORK))
                                }
                            }
                        ))
                    }
                    popupMenu.add(JMenuItem(
                        object : AbstractAction() {
                            override fun actionPerformed(e: ActionEvent) {
                                SwingUtilities.invokeLater {
                                    val parent = mutableTreeNode.parent
                                    mutableTreeNode.removeFromParent()
                                    defaultTreeModel.reload(parent)

                                    expandedSet.forEach { path ->
                                        tree.expandPath(path)
                                    }

                                    ConfigurationUiObserverFactory.observer
                                        .notify(RemoveSettingUiEvent(defaultTreeModel.getChildCount(root)))
                                }
                            }

                            init {
                                putValue(NAME, "Delete")
                                putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.MINUS))
                            }
                        }
                    ))
                    popupMenu.show(e.component, e.x, e.y)
                }
            }
        })
        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun setTitle(dateTime: LocalDateTime) {
        val dateAsString = dateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        frame.title = "Configuring actions '$dateAsString'"
    }

    private fun fillTreeNodes(actions: List<RocketActionSettingsModel>?, parent: DefaultMutableTreeNode) {
        for (rocketActionSettings in actions!!) {
            rocketActionPluginApplicationService.by(type = rocketActionSettings.type)
                ?.configuration(RocketActionContextFactory.context)
                ?.let { config ->
                    val current = DefaultMutableTreeNode(
                        TreeRocketActionSettings(
                            configuration = config,
                            settings = MutableRocketActionSettings.from(rocketActionSettings),
                        ),
                        true
                    )
                    parent.add(current)
                    if (rocketActionSettings.actions.isNotEmpty()) {
                        val childAction = rocketActionSettings.actions
                        fillTreeNodes(actions = childAction, parent = current)
                    }
                } ?: run {
                logger.warn {
                    "Configuration for settings '${rocketActionSettings.type}' " +
                        "not found and skipped"
                }
            }
        }
    }

    private fun saveSettings(treeModel: DefaultTreeModel) {
        val settings: MutableList<MutableRocketActionSettings> = ArrayList()
        val root = treeModel.root as DefaultMutableTreeNode
        val childCount = root.childCount
        for (i in 0 until childCount) {
            recursiveGetSettings(root.getChildAt(i) as DefaultMutableTreeNode, settings, null)
        }
        try {
            val actions = ActionsModel(actions = settings.map { it.toModel() })
            rocketActionSettingsService.save(actions)
            setTitle(actions.lastChangedDate)
            RocketActionContextFactory.context.notification().show(NotificationType.INFO, "Actions saved")
        } catch (e: RocketActionSettingsRepositoryException) {
            e.printStackTrace()
            RocketActionContextFactory.context.notification().show(NotificationType.ERROR, "Error saving actions")
        }
    }

    private fun recursiveGetSettings(
        node: DefaultMutableTreeNode,
        settings: MutableList<MutableRocketActionSettings>,
        parent: MutableRocketActionSettings?
    ) {
        val originalActionSettings = node.userObject as TreeRocketActionSettings
        val finalActionSettings = MutableRocketActionSettings(
            id = originalActionSettings.settings.id,
            type = originalActionSettings.settings.type,
            settings = originalActionSettings.settings.settings,
            tags = originalActionSettings.settings.tags,
        )
        parent?.actions?.add(finalActionSettings) ?: settings.add(finalActionSettings)
        val childCount = node.childCount
        for (i in 0 until childCount) {
            recursiveGetSettings(node.getChildAt(i) as DefaultMutableTreeNode, settings, finalActionSettings)
        }
    }

    fun setVisible(visible: Boolean) {
        checkEmptyActionsAndShowButtonCreate(menuBar!!)

        frame.isVisible = visible
    }

    private inner class RocketActionSettingsCellRender : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any,
            sel: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean
        ): Component {
            val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
            val node = value as DefaultMutableTreeNode
            if (node.userObject is TreeRocketActionSettings) {
                val settings = node.userObject as TreeRocketActionSettings
                label.text = settings.asString()
                settings.configuration.icon()?.let { icon ->
                    label.icon = icon
                }
            }
            return label
        }
    }

    private var menuBar: JToolBar?

    init {
        frame.iconImage = RocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage()
        frame.isAlwaysOnTop = GeneralPropertiesRepositoryFactory.repository
            .asBoolean(UsedPropertiesName.UI_CONFIGURATION_FRAME_ALWAYS_ON_TOP, false)
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        this.updateActionListener = updateActionListener
        this.rocketActionPluginApplicationService = rocketActionPluginApplicationService

        val size = Toolkit.getDefaultToolkit().screenSize
        frame.setSize(
            (size.width * GeneralPropertiesRepositoryFactory
                .repository
                .asFloat(
                    UsedPropertiesName.UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT,
                    0.6F
                )
                ).toInt(),
            (size.height * GeneralPropertiesRepositoryFactory
                .repository
                .asFloat(
                    UsedPropertiesName.UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT,
                    0.6F
                )
                ).toInt()
        )
        frame.setLocationRelativeTo(null)
        createRocketActionSettingsDialog = CreateRocketActionSettingsDialog(
            owner = frame,
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
        )

        val basePanel = JPanel(BorderLayout())

        menuBar = createToolBar()

        basePanel.add(menuBar, BorderLayout.NORTH)
        basePanel.add(panel(), BorderLayout.CENTER)

        frame.add(basePanel, BorderLayout.CENTER)

        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is RemoveSettingUiEvent && event.countChildrenRoot == 0) {
                    createAndShowButtonCreateFirstAction(menuBar = menuBar!!)
                }
            }
        })
    }

    private fun createToolBar(): JToolBar {
        val menuBar = JToolBar()

        // Refresh
        menuBar.add(
            JButton("Refresh").apply {
                icon = RocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
                addActionListener { e: ActionEvent? ->
                    SwingUtilities.invokeLater {
                        updateActionListener.actionPerformed(e)
                        ConfigurationFrame@ frame.isVisible = false
                    }
                }
            })

        // Variables
        menuBar.add(JButton("Variables").apply {
            val variablesFrame = VariablesFrame(frame)
            icon = RocketActionContextFactory.context.icon().by(AppIcon.FORK)
            addActionListener {
                SwingUtilities.invokeLater {
                    variablesFrame.isVisible = true
                }
            }
        })

        // Plugin Download Information
        menuBar.add(JButton("Plugin Download Information").apply {
            val pluginManagerFrame = PluginManagerFrame(
                rocketActionPluginApplicationService = rocketActionPluginApplicationService,
                parent = frame
            )
            icon = RocketActionContextFactory.context.icon().by(AppIcon.INFO)
            addActionListener {
                SwingUtilities.invokeLater {
                    pluginManagerFrame.isVisible = true
                }
            }
        })

        return menuBar
    }

    private
    var buttonCreateNewAction: JButton? = null

    private fun checkEmptyActionsAndShowButtonCreate(menuBar: JToolBar) {
        if (rocketActionSettingsService.actionsModel().actions.isEmpty() && buttonCreateNewAction == null) {
            createAndShowButtonCreateFirstAction(menuBar = menuBar)
        }
    }

    private fun createAndShowButtonCreateFirstAction(menuBar: JToolBar) {
        buttonCreateNewAction = JButton("Create first action")
        buttonCreateNewAction!!.icon = RocketActionContextFactory.context.icon().by(AppIcon.STAR)
        buttonCreateNewAction!!.addActionListener { _: ActionEvent? ->
            createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                override fun create(settings: TreeRocketActionSettings) {
                    val model = finalTree!!.model as DefaultTreeModel
                    val root = model.root as DefaultMutableTreeNode
                    root.add(DefaultMutableTreeNode(settings, true))
                    model.reload(root)
                    menuBar.remove(buttonCreateNewAction)
                    buttonCreateNewAction = null
                    menuBar.repaint()
                }
            })
        }
        menuBar.add(buttonCreateNewAction)
        menuBar.repaint()
        menuBar.revalidate()
    }
}
