package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.*

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
        val rocketActionSettingsPanel = EditorRocketActionSettingsPanel(rocketActionConfigurationRepository)
        val panel = JPanel(BorderLayout())
        val tree = JTree(defaultTreeModel)
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.addTreeSelectionListener { e: TreeSelectionEvent ->
            val path = e.newLeadSelectionPath ?: return@addTreeSelectionListener
            val node = path.lastPathComponent as DefaultMutableTreeNode
                    ?: return@addTreeSelectionListener
            val o = node.userObject
            if (o != null) {
                val settings = o as RocketActionSettings
                SwingUtilities.invokeLater {
                    rocketActionSettingsPanel.show(
                            settings,
                            object : SavedRocketActionSettingsPanelCallback {
                                override fun saved(rocketActionSettings: RocketActionSettings) {
                                    node.userObject = rocketActionSettings
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
        val buttonSaveTree = JButton("Save actions")
        buttonSaveTree.addActionListener { e: ActionEvent? -> saveSettings(defaultTreeModel) }
        panelSaveTree.add(buttonSaveTree)
        panelTree.add(panelSaveTree, BorderLayout.SOUTH)
        splitPane.leftComponent = panelTree
        splitPane.rightComponent = rocketActionSettingsPanel
        tree.dragEnabled = true
        tree.dropMode = DropMode.ON_OR_INSERT
        tree.transferHandler = TreeTransferHandler()
        tree.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                if (e.button == MouseEvent.BUTTON3) {
                    val treePath = tree.getClosestPathForLocation(e.x, e.y) ?: return
                    val mutableTreeNode = treePath.lastPathComponent as DefaultMutableTreeNode
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
                            putValue(NAME, "Add new TOP")
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
                                    putValue(NAME, "Add new DOWN")
                                }
                            }
                    ))
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
                                    putValue(NAME, "Add new as child")
                                }
                            }
                    ))
                    popupMenu.add(JMenuItem(
                            object : AbstractAction() {
                                override fun actionPerformed(e: ActionEvent) {
                                    SwingUtilities.invokeLater {
                                        mutableTreeNode.removeFromParent()
                                        defaultTreeModel.reload()
                                    }
                                }

                                init {
                                    putValue(NAME, "Delete")
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

    private fun fillTreeNodes(actions: List<RocketActionSettings?>?, parent: DefaultMutableTreeNode) {
        for (rocketActionSettings in actions!!) {
            val current = DefaultMutableTreeNode(rocketActionSettings, true)
            parent.add(current)
            if (!rocketActionSettings!!.actions().isEmpty()) {
                val childAction = rocketActionSettings.actions()
                fillTreeNodes(childAction, current)
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
            NotificationFactory.notification.show(NotificationType.INFO, "Actions saved")
        } catch (e: RocketActionSettingsRepositoryException) {
            e.printStackTrace()
            NotificationFactory.notification.show(NotificationType.ERROR, "Error actions saving")
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
            if (node.userObject is RocketActionSettings) {
                val settings = node.userObject as RocketActionSettings
                val labelProperty = settings.settings()["label"]
                if (labelProperty != null && "" != labelProperty) {
                    label.text = labelProperty
                } else {
                    label.text = settings.type()
                }
            }
            return label
        }
    }


    init {
        dialog = JDialog(owner, "Rocket action configuration")
        this.updateActionListener = updateActionListener
        this.rocketActionConfigurationRepository = rocketActionConfigurationRepository
        this.rocketActionUiRepository = rocketActionUiRepository
        this.rocketActionSettingsRepository = rocketActionSettingsRepository
        val menuBar = JMenuBar()
        val menuItemUpdate = JMenuItem("Update")
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