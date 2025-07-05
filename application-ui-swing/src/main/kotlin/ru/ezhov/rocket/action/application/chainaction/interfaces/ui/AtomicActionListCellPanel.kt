package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.toIcon8x8
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer.ActionSchedulerStatusComponentService
import ru.ezhov.rocket.action.plugin.markdown.Markdown
import ru.ezhov.rocket.action.ui.utils.swing.common.toIcon
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AtomicActionListCellPanel(
    atomicAction: AtomicAction,
    backgroundColor: Color? = null,
    chainActionService: ChainActionService,
    actionSchedulerStatusComponentService: ActionSchedulerStatusComponentService,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val iconLabel = JLabel(ATOMIC_ICON, SwingConstants.LEFT)
    private val contractLabel = JLabel(atomicAction.contractType.toIcon8x8())
    private val engineLabel = JLabel(atomicAction.engine.toIcon8x8())
    private val schedulerLabel = JLabel()
    private val nameLabel = JLabel(createName(atomicAction, chainActionService)).apply {
        atomicAction.icon?.let {
            icon = it.toIcon()
        }
    }

    private fun createName(atomicAction: AtomicAction, chainActionService: ChainActionService): String {
        val name = atomicAction.name
        val alias = atomicAction.alias?.let { ". _${it}_" }.orEmpty()
        val usage = ". **${chainActionService.usageAction(atomicAction.id).size}**"

        val text = "$name$usage$alias"
        return "<html>" + Markdown.textMarkdownToHtml(text)
    }

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(iconLabel)
        add(contractLabel)
        add(engineLabel)
        add(actionSchedulerStatusComponentService.component(atomicAction.id()))
        add(nameLabel)
    }
}
