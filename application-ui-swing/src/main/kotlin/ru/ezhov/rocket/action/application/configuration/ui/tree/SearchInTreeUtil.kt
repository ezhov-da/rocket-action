package ru.ezhov.rocket.action.application.configuration.ui.tree

import java.util.*
import javax.swing.tree.DefaultMutableTreeNode

object SearchInTreeUtil {
    fun searchInTree(
        condition: (settings: TreeRocketActionSettings) -> Boolean,
        root: DefaultMutableTreeNode,
    ): List<DefaultMutableTreeNode> {
        val mutableList: MutableList<DefaultMutableTreeNode> = mutableListOf()
        var node: DefaultMutableTreeNode?
        val e: Enumeration<*> = root.breadthFirstEnumeration()
        while (e.hasMoreElements()) {
            node = e.nextElement() as DefaultMutableTreeNode
            if (node.userObject != null && node.userObject is TreeRocketActionSettings) {
                val settings = node.userObject as TreeRocketActionSettings
                if (condition(settings)) {
                    mutableList.add(node)
                }
            }
        }
        return mutableList.toList()
    }
}
