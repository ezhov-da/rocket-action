package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel

class ChainActionListCellPanel(
    chainAction: ChainAction,
    backgroundColor: Color? = null,
) :
    JPanel(MigLayout(/*"debug"*/)) {
    private val nameLabel = JLabel(chainAction.name)
    private val countAtomicAction = JLabel("Actions: ${chainAction.actionIds.size}")

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(nameLabel, "wrap")
        add(countAtomicAction)
    }
}
