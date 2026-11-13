package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.SelectedAtomicAction
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

/**
 * Renders a selected atomic action row from [snapshot] only.
 */
class OrderAtomicActionListCellRenderer(
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
        return if (value is SelectedAtomicAction) {
            AtomicActionListCellPanel(
                atomicAction = value.atomicAction,
                decoration = snapshot[value.atomicAction.id],
                backgroundColor = label.background,
            )
        } else {
            label
        }
    }
}
