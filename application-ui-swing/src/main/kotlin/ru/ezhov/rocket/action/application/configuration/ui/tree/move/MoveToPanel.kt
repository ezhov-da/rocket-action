package ru.ezhov.rocket.action.application.configuration.ui.tree.move

import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ListSelectionModel
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode

class MoveToPanel(
    currentNode: DefaultMutableTreeNode,
    nodes: List<DefaultMutableTreeNode>,
    selectCallback: (DefaultMutableTreeNode) -> Unit,
) : JPanel(BorderLayout()) {
    private val searchTextField = TextFieldWithText("Search")
    private val allNodes = nodes
        .asSequence()
        .filter { it != currentNode } // exclude current node
        .map { ListModel(it) }
        .filter { it.parents().none { par -> par == currentNode } } // exclude children nodes
        .filter { it.node != currentNode.parent } // exclude already parent
        .sortedBy { it.text() }
        .toList()
    private val listModel = DefaultListModel<ListModel>()
    private val list = JList(listModel)

    init {
        fun setValuesToModel(nodes: List<ListModel>) {
            listModel.removeAllElements()
            nodes.forEach {
                listModel.addElement(it)
            }
        }

        setValuesToModel(allNodes)

        searchTextField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                SwingUtilities.invokeLater {
                    if (searchTextField.text.isEmpty()) {
                        setValuesToModel(allNodes)
                    } else {
                        setValuesToModel(allNodes.filter { it.text().contains(searchTextField.text, true) })
                    }
                }
            }
        })


        fun select() {
            val selectedValue = list.selectedValue
            if (selectedValue != null) {
                SwingUtilities.invokeLater {
                    selectCallback(selectedValue.node)
                }
            }
        }

        list.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    select()
                }
            }
        })

        list.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    select()
                }
            }
        })

        list.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        list.cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel

                if (value is ListModel) {
                    label.text = value.text()
                }

                return label
            }
        }

        add(searchTextField, BorderLayout.NORTH)
        add(JScrollPane(list), BorderLayout.CENTER)
    }

    private data class ListModel(
        val node: DefaultMutableTreeNode,
        val settings: TreeRocketActionSettings? = node.userObject as? TreeRocketActionSettings
    ) {
        private var parents: LinkedList<DefaultMutableTreeNode>? = null
        private var textList: LinkedList<String>? = null

        fun parents(): List<DefaultMutableTreeNode> {
            if (parents == null) {
                parents = LinkedList<DefaultMutableTreeNode>()

                fun fillParents(initNode: DefaultMutableTreeNode?) {
                    if (initNode == null) return

                    if (initNode != node) {
                        val settings = initNode.userObject as? TreeRocketActionSettings
                        if (settings != null) {
                            parents!!.push(initNode)
                        }

                        val parent = initNode.parent as? DefaultMutableTreeNode
                        if (parent != null) {
                            fillParents(parent)
                        }
                    }
                }

                fillParents(node.parent as? DefaultMutableTreeNode)
            }

            return parents!!
        }

        fun text(): String {
            if (textList == null) {
                textList = LinkedList<String>()

                fun fillParents(node: DefaultMutableTreeNode) {
                    val settings = node.userObject as? TreeRocketActionSettings
                    if (settings != null) {
                        textList!!.push(settings.asString(null))
                    } else {
                        textList!!.push("Root")
                    }

                    val parent = node.parent as? DefaultMutableTreeNode
                    if (parent != null) {
                        fillParents(parent)
                    }
                }

                fillParents(node)
            }

            return textList!!.joinToString(separator = " -> ")
        }
    }
}
