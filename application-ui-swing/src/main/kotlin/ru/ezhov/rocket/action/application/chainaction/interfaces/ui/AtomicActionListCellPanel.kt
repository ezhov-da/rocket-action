package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.inOutIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.inUnitIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.unitOutIcon
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.unitUnitIcon
import java.awt.Color
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class AtomicActionListCellPanel(
    atomicAction: AtomicAction,
    backgroundColor: Color? = null,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val nameLabel = JLabel(atomicAction.name, ATOMIC_ICON, SwingConstants.LEFT)
    private val contractLabel = JLabel(
        when (atomicAction.contractType) {
            ContractType.IN_OUT -> inOutIcon
            ContractType.IN_UNIT -> inUnitIcon
            ContractType.UNIT_OUT -> unitOutIcon
            ContractType.UNIT_UNIT -> unitUnitIcon
        }
    )
    private val engineLabel = JLabel(atomicAction.engine.name)
    private val sourceLabel = JLabel(atomicAction.source.name)

    init {
        isOpaque = true
        backgroundColor?.let {
            this.background = backgroundColor
        }
        add(nameLabel, "span, grow, wrap")
        add(contractLabel)
        add(engineLabel)
        add(sourceLabel)
    }
}
