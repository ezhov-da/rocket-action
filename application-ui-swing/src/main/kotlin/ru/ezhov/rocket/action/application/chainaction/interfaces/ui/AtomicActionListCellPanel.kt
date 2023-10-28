package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel

class AtomicActionListCellPanel(
    atomicAction: AtomicAction,
    backgroundColor: Color? = null,
) :
    JPanel(MigLayout(/*"debug"*/)) {
    private val nameLabel = JLabel(atomicAction.name)
    private val engineLabel = JLabel(atomicAction.engine.name)
    private val sourceLabel = JLabel(atomicAction.source.name)

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(nameLabel, "span, grow, wrap")
        add(engineLabel)
        add(sourceLabel)
    }
}
