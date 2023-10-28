package ru.ezhov.rocket.action.application.configuration.ui.tree

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.CreateRocketActionSettingsDialog
import ru.ezhov.rocket.action.application.configuration.ui.CreatedRocketActionSettingsCallback
import ru.ezhov.rocket.action.application.configuration.ui.SavedRocketActionSettingsPanelCallback
import ru.ezhov.rocket.action.application.configuration.ui.edit.EditorRocketActionSettingsPanel
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.RemoveSettingUiEvent
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.event.RocketActionSettingsCreatedDomainEvent
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.DropMode
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

private val logger = KotlinLogging.logger {}

class ConfigurationTreePanel(
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsService: RocketActionSettingsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog,
    private val editorRocketActionSettingsPanel: EditorRocketActionSettingsPanel,
) : JPanel(BorderLayout()) {
    private val root = DefaultMutableTreeNode(null, true)
    private val defaultTreeModel = DefaultTreeModel(root)
    private var tree = JTree(defaultTreeModel)
    private val toolbar = JToolBar(JToolBar.VERTICAL)
    private val innerPanelTree = JPanel(BorderLayout())

    init {
        createToolBar()

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                if (event is RocketActionSettingsCreatedDomainEvent) {
                    val model = event.rocketActionSettingsModel
                    val treeModel = create(model)

                    if (event.groupId == null) {
                        root.add(treeModel)
                        defaultTreeModel.reload(root)
                    } else {
                        SearchInTreeUtil.searchInTree(
                            condition = { settings -> settings.settings.id == event.groupId },
                            root = root,
                        )
                            .firstOrNull()
                            ?.let { parent ->
                                parent.add(treeModel)
                                defaultTreeModel.reload(parent)
                            }
                    }
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(RocketActionSettingsCreatedDomainEvent::class.java)

        })

        val actionsModel = rocketActionSettingsService.actionsModel()
        fillTreeNodes(actionsModel.actions, root)
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.cellRenderer = RocketActionSettingsTreeCellRender()
        tree.isRootVisible = false
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON_OR_INSERT
        tree.transferHandler = TreeTransferHandler()

        tree.addTreeSelectionListener { e: TreeSelectionEvent ->
            val path = e.newLeadSelectionPath ?: return@addTreeSelectionListener
            val node = path.lastPathComponent as DefaultMutableTreeNode
            val o = node.userObject
            if (o != null) {
                val settings = o as TreeRocketActionSettings
                SwingUtilities.invokeLater {
                    editorRocketActionSettingsPanel.show(
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
                                            DefaultMutableTreeNode(
                                                settings,
                                                settings.settings.type == GroupRocketActionUi.TYPE
                                            ),
                                            mutableTreeNode.parent as MutableTreeNode,
                                            mutableTreeNode.parent.getIndex(mutableTreeNode)
                                        )
                                    }
                                }
                            })
                        }

                        init {
                            putValue(NAME, "Add above")
                            putValue(SMALL_ICON, rocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                        }
                    }))
                    popupMenu.add(JMenuItem(
                        object : AbstractAction() {
                            override fun actionPerformed(e: ActionEvent) {
                                createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                    override fun create(settings: TreeRocketActionSettings) {
                                        SwingUtilities.invokeLater {
                                            defaultTreeModel.insertNodeInto(
                                                DefaultMutableTreeNode(
                                                    settings,
                                                    settings.settings.type == GroupRocketActionUi.TYPE
                                                ),
                                                mutableTreeNode.parent as MutableTreeNode,
                                                mutableTreeNode.parent.getIndex(mutableTreeNode) + 1
                                            )
                                        }
                                    }

                                })
                            }

                            init {
                                putValue(NAME, "Add below")
                                putValue(SMALL_ICON, rocketActionContextFactory.context.icon().by(AppIcon.PLUS))
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
                                                    DefaultMutableTreeNode(
                                                        settings,
                                                        settings.settings.type == GroupRocketActionUi.TYPE
                                                    )
                                                )
                                                defaultTreeModel.reload(mutableTreeNode)
                                            }
                                        }
                                    })
                                }

                                init {
                                    putValue(NAME, "Create and add as child")
                                    putValue(SMALL_ICON, rocketActionContextFactory.context.icon().by(AppIcon.PLUS))
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
                                                false
                                            ),
                                            mutableTreeNode.parent as MutableTreeNode,
                                            mutableTreeNode.parent.getIndex(mutableTreeNode) + 1
                                        )
                                    }
                                }

                                init {
                                    putValue(NAME, "Duplicate")
                                    putValue(SMALL_ICON, rocketActionContextFactory.context.icon().by(AppIcon.FORK))
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
                                        .notify(
                                            RemoveSettingUiEvent(
                                                defaultTreeModel.getChildCount(root),
                                                (mutableTreeNode.userObject as TreeRocketActionSettings)
                                            )
                                        )
                                }
                            }

                            init {
                                putValue(NAME, "Delete")
                                putValue(SMALL_ICON, rocketActionContextFactory.context.icon().by(AppIcon.MINUS))
                            }
                        }
                    ))
                    popupMenu.show(e.component, e.x, e.y)
                }
            }
        })

        innerPanelTree.add(
            SearchInTreePanel(
                root = root,
                treeModel = defaultTreeModel,
                tree = tree,
                rocketActionContextFactory = rocketActionContextFactory
            ),
            BorderLayout.NORTH
        )
        innerPanelTree.add(JScrollPane(tree), BorderLayout.CENTER)

        add(innerPanelTree, BorderLayout.CENTER)
        add(toolbar, BorderLayout.WEST)

        val panelSaveTree = JPanel()
        val buttonSaveTree = JButton(
            "Save all configuration to storage",
            rocketActionContextFactory.context.icon().by(AppIcon.SAVE)
        )
        buttonSaveTree.addActionListener { saveSettings(defaultTreeModel) }
        panelSaveTree.add(buttonSaveTree)

        defaultTreeModel.reload()

        add(panelSaveTree, BorderLayout.SOUTH)
    }

    private fun createToolBar() {
        toolbar.apply {
            isFloatable = false
            isRollover = false
            add(
                JButton()
                    .apply {
                        val expandIcon = rocketActionContextFactory.context.icon().by(AppIcon.EXPAND)
                        val collapseIcon = rocketActionContextFactory.context.icon().by(AppIcon.COLLAPSE)
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

            add(
                JButton()
                    .apply {
                        toolTipText = "Add a new action configuration to the end of root"
                        icon = ImageIcon(this::class.java.getResource("/icons/add_16x16.png"))

                        addActionListener {
                            createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                override fun create(settings: TreeRocketActionSettings) {
                                    SwingUtilities.invokeLater {
                                        root.add(
                                            DefaultMutableTreeNode(
                                                settings, settings.settings.type == GroupRocketActionUi.TYPE
                                            )
                                        )
                                        defaultTreeModel.reload(root)
                                    }
                                }
                            })
                        }
                    }
            )
        }
    }

    private fun fillTreeNodes(actions: List<RocketActionSettingsModel>?, parent: DefaultMutableTreeNode) {
        for (rocketActionSettings in actions!!) {
            create(rocketActionSettings)?.let { current ->
                parent.add(current)
                if (rocketActionSettings.actions.isNotEmpty()) {
                    val childAction = rocketActionSettings.actions
                    fillTreeNodes(actions = childAction, parent = current)
                }
            }
        }
    }

    private fun create(rocketActionSettings: RocketActionSettingsModel): DefaultMutableTreeNode? =
        rocketActionPluginApplicationService.by(type = rocketActionSettings.type)
            ?.configuration(rocketActionContextFactory.context)
            ?.let { config ->
                val current = DefaultMutableTreeNode(
                    TreeRocketActionSettings(
                        configuration = config,
                        settings = MutableRocketActionSettings.from(rocketActionSettings),
                    ),
                    rocketActionSettings.type == GroupRocketActionUi.TYPE
                )

                current
            } ?: run {
            logger.warn {
                "Configuration for settings '${rocketActionSettings.type}' " +
                    "not found and skipped"
            }

            null
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
            rocketActionContextFactory.context.notification().show(NotificationType.INFO, "Actions saved")
        } catch (e: RocketActionSettingsRepositoryException) {
            e.printStackTrace()
            rocketActionContextFactory.context.notification().show(NotificationType.ERROR, "Error saving actions")
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
}
