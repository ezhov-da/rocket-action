package ru.ezhov.rocket.action.application.configuration.ui.tree

import java.util.*
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

// https://www.logicbig.com/tutorials/java-swing/jtree-expand-collapse-all-nodes.html
object JTreeUtil {
    fun setTreeExpandedState(tree: JTree, expanded: Boolean) {
        val node = tree.model.root as DefaultMutableTreeNode
        setNodeExpandedState(tree, node, expanded)
    }

    private fun setNodeExpandedState(tree: JTree, node: DefaultMutableTreeNode, expanded: Boolean) {
        val enumeration: Enumeration<DefaultMutableTreeNode> = node.children() as Enumeration<DefaultMutableTreeNode>
        for (treeNode in enumeration) {
            setNodeExpandedState(tree, treeNode, expanded)
        }
        if (!expanded && node.isRoot) {
            return
        }
        val path = TreePath(node.path)
        if (expanded) {
            tree.expandPath(path)
        } else {
            tree.collapsePath(path)
        }
    }
}
