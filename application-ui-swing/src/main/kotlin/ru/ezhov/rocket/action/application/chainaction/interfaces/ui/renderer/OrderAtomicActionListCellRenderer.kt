package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.SelectedAtomicAction
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

class OrderAtomicActionListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        return if (value is SelectedAtomicAction) {
            AtomicActionListCellPanel(value.atomicAction, label.background)
        } else {
            label
        }
    }
}
