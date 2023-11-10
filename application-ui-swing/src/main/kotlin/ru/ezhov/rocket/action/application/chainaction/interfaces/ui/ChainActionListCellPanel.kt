package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.iconForContractTypes
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class ChainActionListCellPanel(
    chainAction: ChainAction,
    backgroundColor: Color? = null,
    firstAtomicAction: AtomicAction?,
    lastAtomicAction: AtomicAction?,
) :
    JPanel(MigLayout(/*"debug"*/)) {
    private val nameLabel = JLabel(chainAction.name, CHAIN_ICON, SwingConstants.LEFT)
    private val contractLabel: JLabel? = iconForContractTypes(
        first = firstAtomicAction?.contractType,
        second = lastAtomicAction?.contractType
    )
        ?.let { JLabel(it) }
    private val countAtomicAction = JLabel("Actions: ${chainAction.actions.size}")

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(nameLabel, "span")
        contractLabel?.let { add(it) }
        add(countAtomicAction)
    }
}
