package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.inOutIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.inUnitIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.unitOutIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.unitUnitIcon
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class ChainActionListCellPanel(
    chainAction: ChainAction,
    backgroundColor: Color? = null,
    firstAtomicAction: AtomicAction?
) :
    JPanel(MigLayout(/*"debug"*/)) {
    private val nameLabel = JLabel(chainAction.name, CHAIN_ICON, SwingConstants.LEFT)
    private val contractLabel: JLabel? = firstAtomicAction?.let {
        JLabel(
            when (it.contractType) {
                ContractType.IN_OUT -> inOutIcon
                ContractType.IN_UNIT -> inUnitIcon
                ContractType.UNIT_OUT -> unitOutIcon
                ContractType.UNIT_UNIT -> unitUnitIcon
            }
        )
    }
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
