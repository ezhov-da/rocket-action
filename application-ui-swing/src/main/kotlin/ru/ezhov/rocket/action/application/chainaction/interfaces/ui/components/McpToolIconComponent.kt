package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components

import ru.ezhov.rocket.action.application.resources.Icons
import java.awt.Component
import javax.swing.Box
import javax.swing.JLabel

/**
 * Visual marker for actions published as MCP tools.
 *
 * The placeholder keeps list cells aligned when the action is not a tool.
 */
object McpToolIconComponent {
    fun component(isTool: Boolean): Component =
        if (isTool) {
            JLabel(Icons.Standard.CLOUD_8x8).apply {
                toolTipText = "Used for MCP"
            }
        } else {
            Box.createHorizontalStrut(8)
        }
}
