package ru.ezhov.rocket.action.application.configuration.ui.tree

import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiListener
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.eventui.model.SettingMovedUiEvent
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath

class SearchInTreePanel(
    private val root: DefaultMutableTreeNode,
    private val treeModel: DefaultTreeModel,
    private val tree: JTree,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val searchTextTransformer: SearchTextTransformer,
) : JPanel() {
    private val textField = TextFieldWithText("Search")
    private var currentResultPanel: ResultPanel? = null

    init {
        this.layout = BorderLayout()
        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is SettingMovedUiEvent) {
                    SwingUtilities.invokeLater { resetSearch(this@SearchInTreePanel) }
                }
            }
        })
        val searchInTreePanel = this
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (textField.text.isNotEmpty() && e.keyCode == KeyEvent.VK_ENTER) {
                    val searchText = searchTextTransformer.transformedText(textField.text)

                    val nodes = SearchInTreeUtil.searchInTree(
                        condition = { settings ->
                            searchText.any { st ->
                                settings.settings.settings.any { set ->
                                    set.value.contains(
                                        other = st,
                                        ignoreCase = true
                                    )
                                }
                            }
                        },
                        root = root,
                    )
                    if (nodes.isNotEmpty()) {
                        if (currentResultPanel != null) {
                            searchInTreePanel.remove(currentResultPanel)
                        }
                        currentResultPanel = ResultPanel(
                            foundNodes = nodes,
                            treeModel = treeModel,
                            tree = tree,
                            rocketActionContextFactory = rocketActionContextFactory,
                        )
                        searchInTreePanel.add(currentResultPanel, BorderLayout.EAST)
                        searchInTreePanel.repaint()
                        searchInTreePanel.revalidate()
                    }
                } else if (textField.text.isEmpty()) {
                    resetSearch(searchInTreePanel)
                }
            }
        })
        this.add(textField, BorderLayout.CENTER)
    }

    private fun resetSearch(searchInTreePanel: SearchInTreePanel) {
        if (currentResultPanel != null) {
            searchInTreePanel.remove(currentResultPanel)
        }
        currentResultPanel = null
        searchInTreePanel.repaint()
        searchInTreePanel.revalidate()
    }

    private class ResultPanel(
        private val foundNodes: List<DefaultMutableTreeNode>,
        private val treeModel: DefaultTreeModel,
        private val tree: JTree,
        private val rocketActionContextFactory: RocketActionContextFactory,
    ) : JPanel() {
        private var counter = 1
        private val buttonNext = JButton()
            .apply {
                icon = rocketActionContextFactory.context.icon().by(AppIcon.ARROW_BOTTOM)
                toolTipText = "Next"
            }
        private val buttonPrevious = JButton()
            .apply {
                icon = rocketActionContextFactory.context.icon().by(AppIcon.ARROW_TOP)
                toolTipText = "Previous"
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
