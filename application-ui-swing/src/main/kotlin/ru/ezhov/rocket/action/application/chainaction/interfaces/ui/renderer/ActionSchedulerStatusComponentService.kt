package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.application.resources.Icons
import tips4java.CompoundIcon
import java.awt.Component
import javax.swing.Box
import javax.swing.JLabel

class ActionSchedulerStatusComponentService(
    private val actionSchedulerService: ActionSchedulerService
) {
    fun component(actionId: String): Component =
        actionSchedulerService.scheduler(actionId)?.let { sch ->
            JLabel(
                CompoundIcon(
                    Icons.Standard.CLOCK_8x8, when (sch.actionScheduler.schedulerStatus) {
                        ActionSchedulerStatus.NOT_LAUNCHED -> Icons.Standard.CLOCK_8x8
                        ActionSchedulerStatus.ERROR -> Icons.Standard.BAN_8x8
                        ActionSchedulerStatus.SUCCESS -> Icons.Standard.CHECK_8x8
                    }
                )
            )
        } ?: run {
            Box.createHorizontalStrut(8)
        }

}
