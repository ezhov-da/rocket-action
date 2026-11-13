package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.McpToolIconComponent
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecoration
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionSchedulerStatusComponentService
import ru.ezhov.rocket.action.ui.utils.swing.common.toIcon
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class ChainActionListCellPanel(
    chainAction: ChainAction,
    decoration: ActionRowDecoration? = null,
    backgroundColor: Color? = null,
) :
    JPanel(MigLayout(/*"debug"*/)) {
    private val iconLabel = JLabel(CHAIN_ICON, SwingConstants.LEFT)
    private val nameLabel = JLabel(decoration?.titleHtml ?: chainAction.name).apply {
        chainAction.icon?.let {
            icon = it.toIcon()
        }
    }
    private val contractLabel: JLabel? = decoration?.chainIcon?.let { JLabel(it) }
    private val countAtomicAction = JLabel("<html><b>(${chainAction.actions.size})</b>")

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(iconLabel)
        contractLabel?.let { add(it) }
        add(McpToolIconComponent.component(chainAction.isTool))
        add(ActionSchedulerStatusComponentService.component(decoration?.schedulerStatus))
        add(nameLabel)
        add(countAtomicAction)
    }
}
