package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainActionListCellPanel
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

class ChainAndAtomicActionListCellRenderer(
    private val atomicActionService: AtomicActionService
) : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        return when (value) {
            is ChainAction -> ChainActionListCellPanel(
                chainAction = value,
                backgroundColor = label.background,
                firstAtomicAction = value.actions.firstOrNull()?.let { actionOrder ->
                    atomicActionService.atomicBy(actionOrder.actionId)
                }
            )

            is AtomicAction -> AtomicActionListCellPanel(value, label.background)
            else -> label
        }
    }
}
