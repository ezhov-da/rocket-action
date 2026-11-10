package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import javax.swing.JMenu

class ActionSchedulerJMenu(
    actionId: String,
    actionSchedulerService: ActionSchedulerService
) : JMenu("Scheduler") {
    init {
        val schedulerPanel = ActionSchedulerPanel(actionId, actionSchedulerService)
        SizeUtil.setAllSize(schedulerPanel, SizeUtil.dimension(0.3, 0.3))
        add(schedulerPanel)
    }
}
