package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.AtomicActionListCellPanel
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList

class AtomicActionListCellRenderer(
    private val chainActionService: ChainActionService,
    private val actionSchedulerService: ActionSchedulerService,
) : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
        return if (value is AtomicAction) {
            AtomicActionListCellPanel(
                atomicAction = value,
                backgroundColor = label.background,
                chainActionService = chainActionService,
                actionSchedulerStatusComponentService = ActionSchedulerStatusComponentService(actionSchedulerService),
            )
        } else {
            label
        }
    }
}
