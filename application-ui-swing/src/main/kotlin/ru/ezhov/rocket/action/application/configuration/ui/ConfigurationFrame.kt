package ru.ezhov.rocket.action.application.configuration.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiListener
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.configuration.ui.event.model.RemoveSettingUiEvent
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
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
    rocketActionPluginRepository: RocketActionPluginRepository,
    private val rocketActionSettingsRepository: RocketActionSettingsRepository,
    updateActionListener: ActionListener
) {
    private val frame: JFrame = JFrame("Конфигурирование действий")
    private val rocketActionPluginRepository: RocketActionPluginRepository
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog
    private val updateActionListener: ActionListener
    private var finalTree: JTree? = null

    @Throws(Exception::class)
    private fun panel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(tree(), BorderLayout.CENTER)
        return panel
    }

    @Throws(Exception::class)
    private fun tree(): JPanel {
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        splitPane.setDividerLocation(0.5)
        splitPane.resizeWeight = 0.5
        val actions = rocketActionSettingsRepository.actions()
        val root = DefaultMutableTreeNode(null, true)
        fillTreeNodes(actions, root)
        val defaultTreeModel = DefaultTreeModel(root)
        val rocketActionSettingsPanel = EditorRocketActionSettingsPanel(
            rocketActionPluginRepository = rocketActionPluginRepository,
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
                            }
                        }
                    )
                }
            }
        }
        tree.cellRenderer = RocketActionSettingsCellRender()
        tree.isRootVisible = false
        val panelTree = JPanel(BorderLayout())
        panelTree.add(SearchInTreePanel(root = root, treeModel = defaultTreeModel, tree = tree), BorderLayout.NORTH)
        panelTree.add(JScrollPane(tree), BorderLayout.CENTER)
        val panelSaveTree = JPanel()
        val buttonSaveTree = JButton("Сохранить всю конфигурацию")
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
                            putValue(NAME, "Добавить выше")
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
                                putValue(NAME, "Добавить ниже")
                                putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                            }
                        }
                    ))
                    if (userObject?.settings?.type()?.value() == GroupRocketActionUi.TYPE) {
                        popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                        override fun create(settings: TreeRocketActionSettings) {
                                            SwingUtilities.invokeLater {
                                                mutableTreeNode.add(
                                                    DefaultMutableTreeNode(settings, true))
                                                defaultTreeModel.reload(mutableTreeNode)
                                            }
                                        }
                                    })
                                }

                                init {
                                    putValue(NAME, "Создать и добавить как потомка")
                                    putValue(SMALL_ICON, RocketActionContextFactory.context.icon().by(AppIcon.PLUS))
                                }
                            }
                        ))
                    }
                    if (
                        userObject != null &&
                        userObject.settings.type().value() != GroupRocketActionUi.TYPE &&
                        userObject.settings is MutableRocketActionSettings
                    ) {
                        popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    val settings = userObject.settings
                                    val duplicate = settings.copy(userObject.configuration)
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
                                    putValue(NAME, "Дублировать")
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
                                putValue(NAME, "Удалить")
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

    private fun fillTreeNodes(actions: List<RocketActionSettings>?, parent: DefaultMutableTreeNode) {
        for (rocketActionSettings in actions!!) {
            rocketActionPluginRepository.by(type = rocketActionSettings.type())
                ?.configuration(RocketActionContextFactory.context)
                ?.let { config ->
                    val current = DefaultMutableTreeNode(
                        TreeRocketActionSettings(
                            configuration = config,
                            settings = rocketActionSettings,
                        ),
                        true
                    )
                    parent.add(current)
                    if (rocketActionSettings.actions().isNotEmpty()) {
                        val childAction = rocketActionSettings.actions()
                        fillTreeNodes(actions = childAction, parent = current)
                    }
                } ?: run {
                logger.warn {
                    "Configuration for settings '${rocketActionSettings.type().value()}' " +
                        "not found and skipped"
                }
            }
        }
    }

    private fun saveSettings(treeModel: DefaultTreeModel) {
        val settings: MutableList<RocketActionSettings> = ArrayList()
        val root = treeModel.root as DefaultMutableTreeNode
        val childCount = root.childCount
        for (i in 0 until childCount) {
            recursiveGetSettings(root.getChildAt(i) as DefaultMutableTreeNode, settings, null)
        }
        try {
            rocketActionSettingsRepository.save(settings)
            RocketActionContextFactory.context.notification().show(NotificationType.INFO, "Действия сохранены")
        } catch (e: RocketActionSettingsRepositoryException) {
            e.printStackTrace()
            RocketActionContextFactory.context.notification().show(NotificationType.ERROR, "Ошибка сохранения действий")
        }
    }

    private fun recursiveGetSettings(node: DefaultMutableTreeNode, settings: MutableList<RocketActionSettings>, parent: MutableRocketActionSettings?) {
        val originalActionSettings = node.userObject as TreeRocketActionSettings
        val finalActionSettings = MutableRocketActionSettings(
            id = originalActionSettings.settings.id(),
            type = originalActionSettings.settings.type(),
            settings = originalActionSettings.settings.settings().toMutableMap()
        )
        parent?.add(finalActionSettings) ?: settings.add(finalActionSettings)
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
        override fun getTreeCellRendererComponent(tree: JTree, value: Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
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
        this.rocketActionPluginRepository = rocketActionPluginRepository

        val size = Toolkit.getDefaultToolkit().screenSize
        frame.setSize(
            (size.width * GeneralPropertiesRepositoryFactory
                .repository
                .asFloat(UsedPropertiesName.UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT,
                    0.6F)
                ).toInt(),
            (size.height * GeneralPropertiesRepositoryFactory
                .repository
                .asFloat(UsedPropertiesName.UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT,
                    0.6F)
                ).toInt()
        )
        frame.setLocationRelativeTo(null)
        createRocketActionSettingsDialog = CreateRocketActionSettingsDialog(
            owner = frame,
            rocketActionPluginRepository = rocketActionPluginRepository,
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
        val buttonUpdate = JButton("Обновить")
        buttonUpdate.icon = RocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
        buttonUpdate.addActionListener { e: ActionEvent? ->
            SwingUtilities.invokeLater {
                updateActionListener.actionPerformed(e)
                setVisible(false)
            }
        }
        menuBar.add(buttonUpdate)

        return menuBar
    }

    private
    var buttonCreateNewAction: JButton? = null

    private fun checkEmptyActionsAndShowButtonCreate(menuBar: JToolBar) {
        if (rocketActionSettingsRepository.actions().isEmpty() && buttonCreateNewAction == null) {
            createAndShowButtonCreateFirstAction(menuBar = menuBar)
        }
    }

    private fun createAndShowButtonCreateFirstAction(menuBar: JToolBar) {
        buttonCreateNewAction = JButton("Создать первое действие")
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
