package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.Enumeration
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class SearchInTreePanel(
    private val root: DefaultMutableTreeNode,
    private val treeModel: DefaultTreeModel,
    private val tree: JTree,
) : JPanel() {
    private val textField = TextFieldWithText("Поиск")
    private var currentResultPanel: ResultPanel? = null

    init {
        this.layout = BorderLayout()
        val searchInTreePanel = this
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (textField.text.isNotEmpty() && e.keyCode == KeyEvent.VK_ENTER) {
                    val nodes = searchInTree(text = textField.text, root = root)
                    if (nodes.isNotEmpty()) {
                        if (currentResultPanel != null) {
                            searchInTreePanel.remove(currentResultPanel)
                        }
                        currentResultPanel = ResultPanel(foundNodes = nodes, treeModel = treeModel, tree = tree)
                        searchInTreePanel.add(currentResultPanel, BorderLayout.EAST)
                        searchInTreePanel.repaint()
                        searchInTreePanel.revalidate()
                    }
                } else if (textField.text.isEmpty()) {
                    if (currentResultPanel != null) {
                        searchInTreePanel.remove(currentResultPanel)
                    }
                    currentResultPanel = null
                    searchInTreePanel.repaint()
                    searchInTreePanel.revalidate()
                }
            }
        })
        this.add(textField, BorderLayout.CENTER)
    }

    private fun searchInTree(
        text: String,
        root: DefaultMutableTreeNode,
    ): List<DefaultMutableTreeNode> {
        val mutableList: MutableList<DefaultMutableTreeNode> = mutableListOf()
        var node: DefaultMutableTreeNode? = null
        val e: Enumeration<*> = root.breadthFirstEnumeration()
        while (e.hasMoreElements()) {
            node = e.nextElement() as DefaultMutableTreeNode
            if (node!!.userObject != null && node!!.userObject is TreeRocketActionSettings) {
                val settings = node!!.userObject as TreeRocketActionSettings
                val contains = settings.settings.settings()
                    .filter { e -> e.value.contains(other = text, ignoreCase = true) }
                if (contains.isNotEmpty()) {
                    mutableList.add(node)
                }
            }
        }
        return mutableList.toList()
    }

    private class ResultPanel(
        private val foundNodes: List<DefaultMutableTreeNode>,
        private val treeModel: DefaultTreeModel,
        private val tree: JTree,
    ) : JPanel() {
        private var counter = 1
        private val buttonNext = JButton()
            .apply {
                icon = IconRepositoryFactory.repository.by(AppIcon.ARROW_BOTTOM)
                toolTipText = "Далее"
            }
        private val buttonPrevious = JButton()
            .apply {
                icon = IconRepositoryFactory.repository.by(AppIcon.ARROW_TOP)
                toolTipText = "Назад"
            }
        private val label = JLabel()

        init {
            this.add(buttonNext)
            this.add(buttonPrevious)
            this.add(label)

            buttonNext.addActionListener {
                if ((counter + 1) > foundNodes.size) {
                    counter = 1
                } else {
                    counter += 1
                }
                initSelected(counter)
            }

            buttonPrevious.addActionListener {
                if ((counter - 1) == 0) {
                    counter = foundNodes.size
                } else {
                    counter -= 1
                }
                initSelected(counter)
            }

            initSelected(counter)
        }

        private fun initSelected(number: Int) {
            val nodes: Array<TreeNode> = treeModel.getPathToRoot(foundNodes[number - 1])
            val path = TreePath(nodes)
            tree.scrollPathToVisible(path)
            tree.selectionPath = path
            label.text = "$counter/${foundNodes.size}"
        }
    }
}