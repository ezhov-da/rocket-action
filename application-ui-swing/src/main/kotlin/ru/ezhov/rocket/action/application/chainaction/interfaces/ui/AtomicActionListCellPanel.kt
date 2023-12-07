package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.toIcon8x8
import ru.ezhov.rocket.action.plugin.markdown.Markdown
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AtomicActionListCellPanel(
    atomicAction: AtomicAction,
    backgroundColor: Color? = null,
    chainActionService: ChainActionService,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val iconLabel = JLabel(ATOMIC_ICON, SwingConstants.LEFT)
    private val contractLabel = JLabel(atomicAction.contractType.toIcon8x8())
    private val engineLabel = JLabel(atomicAction.engine.toIcon8x8())
    private val sourceLabel = JLabel(atomicAction.source.toIcon8x8())
    private val nameLabel = JLabel(createName(atomicAction, chainActionService))

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
        add(sourceLabel)
        add(nameLabel)
    }
}
