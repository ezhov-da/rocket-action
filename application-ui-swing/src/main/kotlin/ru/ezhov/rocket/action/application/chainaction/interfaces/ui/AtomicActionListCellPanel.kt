package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.McpToolIconComponent
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.toIcon8x8
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionRowDecoration
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionSchedulerStatusComponentService
import ru.ezhov.rocket.action.ui.utils.swing.common.toIcon
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AtomicActionListCellPanel(
    atomicAction: AtomicAction,
    decoration: ActionRowDecoration? = null,
    backgroundColor: Color? = null,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val iconLabel = JLabel(ATOMIC_ICON, SwingConstants.LEFT)
    private val contractLabel = JLabel(atomicAction.contractType.toIcon8x8())
    private val engineLabel = JLabel(atomicAction.engine.toIcon8x8())
    private val schedulerLabel = JLabel()
    private val nameLabel = JLabel(decoration?.titleHtml ?: atomicAction.name).apply {
        atomicAction.icon?.let {
            icon = it.toIcon()
        }
    }

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(iconLabel)
        add(contractLabel)
        add(engineLabel)
        add(McpToolIconComponent.component(atomicAction.isTool))
        add(ActionSchedulerStatusComponentService.component(decoration?.schedulerStatus))
        add(nameLabel)
    }
}
