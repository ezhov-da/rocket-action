package ru.ezhov.rocket.action.configuration.ui

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.group.GroupRocketActionUi
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.DropMode
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JMenuBar
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JSplitPane
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
        owner: Dialog,
        rocketActionConfigurationRepository: RocketActionConfigurationRepository,
        rocketActionUiRepository: RocketActionUiRepository,
        rocketActionSettingsRepository: RocketActionSettingsRepository?,
        updateActionListener: ActionListener
) {
    private val dialog: JDialog
    private val rocketActionConfigurationRepository: RocketActionConfigurationRepository
    private val rocketActionUiRepository: RocketActionUiRepository
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog
    private val rocketActionSettingsRepository: RocketActionSettingsRepository?
    private val updateActionListener: ActionListener

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
        val actions = rocketActionSettingsRepository!!.actions()
        val root = DefaultMutableTreeNode(null, true)
        fillTreeNodes(actions, root)
        val defaultTreeModel = DefaultTreeModel(root)
        val rocketActionSettingsPanel = EditorRocketActionSettingsPanel(
                rocketActionConfigurationRepository,
                rocketActionUiRepository
        )
        val panel = JPanel(BorderLayout())
        val tree = JTree(defaultTreeModel)
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
                    val userObject = mutableTreeNode.userObject as? RocketActionSettings
                    val popupMenu = JPopupMenu()
                    popupMenu.add(JMenuItem(object : AbstractAction() {
                        override fun actionPerformed(e: ActionEvent) {
                            createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                override fun create(rocketActionSettings: RocketActionSettings) {
                                    SwingUtilities.invokeLater {
                                        val newActionSettings = MutableRocketActionSettings(
                                                rocketActionSettings.id(),
                                                rocketActionSettings.type(),
                                                rocketActionSettings.settings().toMutableMap(),
                                                rocketActionSettings.actions().toMutableList()
                                        )
                                        defaultTreeModel.insertNodeInto(
                                                DefaultMutableTreeNode(newActionSettings, true),
                                                mutableTreeNode.parent as MutableTreeNode,
                                                mutableTreeNode.parent.getIndex(mutableTreeNode)
                                        )
                                    }
                                }
                            })
                        }

                        init {
                            putValue(NAME, "Добавить выше")
                        }
                    }))
                    popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                        override fun create(rocketActionSettings: RocketActionSettings) {
                                            SwingUtilities.invokeLater {
                                                defaultTreeModel.insertNodeInto(
                                                        DefaultMutableTreeNode(
                                                                MutableRocketActionSettings(
                                                                        rocketActionSettings.id(),
                                                                        rocketActionSettings.type(),
                                                                        rocketActionSettings.settings().toMutableMap(),
                                                                        rocketActionSettings.actions().toMutableList()
                                                                ),
                                                                true
                                                        ),
                                                        mutableTreeNode.parent as MutableTreeNode,
                                                        mutableTreeNode.parent.getIndex(mutableTreeNode) + 1
                                                )
                                            }
                                        }

                                    })
                                }

                                init {
                                    putValue(NAME, "Добавить ниже")
                                }
                            }
                    ))
                    if (userObject?.type()?.value() == GroupRocketActionUi.TYPE) {
                        popupMenu.add(JMenuItem(
                                object : AbstractAction() {
                                    override fun actionPerformed(e: ActionEvent) {
                                        createRocketActionSettingsDialog.show(object : CreatedRocketActionSettingsCallback {
                                            override fun create(rocketActionSettings: RocketActionSettings) {
                                                SwingUtilities.invokeLater {
                                                    mutableTreeNode.add(
                                                            DefaultMutableTreeNode(
                                                                    MutableRocketActionSettings(
                                                                            rocketActionSettings.id(),
                                                                            rocketActionSettings.type(),
                                                                            rocketActionSettings.settings().toMutableMap(),
                                                                            rocketActionSettings.actions().toMutableList()
                                                                    ),
                                                                    true
                                                            )
                                                    )
                                                    defaultTreeModel.reload(mutableTreeNode)
                                                }
                                            }
                                        })
                                    }

                                    init {
                                        putValue(NAME, "Создать и добавить как потомка")
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
                                    }
                                }

                                init {
                                    putValue(NAME, "Удалить")
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
            rocketActionConfigurationRepository.by(rocketActionSettings.type())?.let { config ->
                val current = DefaultMutableTreeNode(
                        TreeRocketActionSettings(
                                config,
                                rocketActionSettings,
                        ),
                        true
                )
                parent.add(current)
                if (rocketActionSettings.actions().isNotEmpty()) {
                    val childAction = rocketActionSettings.actions()
                    fillTreeNodes(childAction, current)
                }
            } ?: run {
                logger.warn { "Configuration for settings '${rocketActionSettings.type()}' not found and skipped" }
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
            rocketActionSettingsRepository!!.save(settings)
            NotificationFactory.notification.show(NotificationType.INFO, "Действия сохранены")
        } catch (e: RocketActionSettingsRepositoryException) {
            e.printStackTrace()
            NotificationFactory.notification.show(NotificationType.ERROR, "Ошибка сохранения действий")
        }
    }

    private fun recursiveGetSettings(node: DefaultMutableTreeNode, settings: MutableList<RocketActionSettings>, parent: MutableRocketActionSettings?) {
        val originalActionSettings = node.userObject as MutableRocketActionSettings
        val finalActionSettings = MutableRocketActionSettings(
                originalActionSettings.id(),
                originalActionSettings.type(),
                originalActionSettings.settings()
        )
        parent?.add(finalActionSettings) ?: settings.add(finalActionSettings)
        val childCount = node.childCount
        for (i in 0 until childCount) {
            recursiveGetSettings(node.getChildAt(i) as DefaultMutableTreeNode, settings, finalActionSettings)
        }
    }

    fun setVisible(visible: Boolean) {
        dialog.isVisible = visible
    }

    private inner class RocketActionSettingsCellRender : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(tree: JTree, value: Any, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component {
            val label = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus) as JLabel
            val node = value as DefaultMutableTreeNode
            if (node.userObject is TreeRocketActionSettings) {
                val settings = node.userObject as TreeRocketActionSettings
                label.text = settings.asString()
            }
            return label
        }
    }


    init {
        dialog = JDialog(owner, "Конфигурирование действий")
        this.updateActionListener = updateActionListener
        this.rocketActionConfigurationRepository = rocketActionConfigurationRepository
        this.rocketActionUiRepository = rocketActionUiRepository
        this.rocketActionSettingsRepository = rocketActionSettingsRepository
        val menuBar = JMenuBar()
        val menuItemUpdate = JMenuItem("Обновить")
        menuItemUpdate.icon = IconRepositoryFactory.repository.by(AppIcon.RELOAD)
        menuItemUpdate.addActionListener { e: ActionEvent? ->
            SwingUtilities.invokeLater {
                updateActionListener.actionPerformed(e)
                setVisible(false)
            }
        }
        menuBar.add(menuItemUpdate)
        val size = Toolkit.getDefaultToolkit().screenSize
        dialog.setSize((size.width * 0.6).toInt(), (size.height * 0.6).toInt())
        dialog.setLocationRelativeTo(null)
        createRocketActionSettingsDialog = CreateRocketActionSettingsDialog(
                dialog,
                rocketActionConfigurationRepository,
                rocketActionUiRepository
        )
        dialog.add(menuBar, BorderLayout.NORTH)
        dialog.add(panel(), BorderLayout.CENTER)
    }
}