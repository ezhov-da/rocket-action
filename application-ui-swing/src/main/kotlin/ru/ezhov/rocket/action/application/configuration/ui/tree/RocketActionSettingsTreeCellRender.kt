package ru.ezhov.rocket.action.application.configuration.ui.tree

import java.awt.Component
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

class RocketActionSettingsTreeCellRender : DefaultTreeCellRenderer() {
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
