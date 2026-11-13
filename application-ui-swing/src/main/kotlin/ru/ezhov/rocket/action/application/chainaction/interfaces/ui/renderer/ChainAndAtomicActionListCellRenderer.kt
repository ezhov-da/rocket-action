package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainActionListCellPanel
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

/**
 * Renders chain and atomic action rows from [snapshot] only: no service calls and no file reads.
 */
class ChainAndAtomicActionListCellRenderer(
    var snapshot: ActionRowDecorationSnapshot = ActionRowDecorationSnapshot.EMPTY,
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
                decoration = snapshot[value.id],
                backgroundColor = label.background,
            )

            is AtomicAction -> AtomicActionListCellPanel(
                atomicAction = value,
                decoration = snapshot[value.id],
                backgroundColor = label.background,
            )

            else -> label
        }
    }
}
