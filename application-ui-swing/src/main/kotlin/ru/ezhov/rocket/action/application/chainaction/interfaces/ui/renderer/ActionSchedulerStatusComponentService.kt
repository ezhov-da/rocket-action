package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.application.resources.Icons
import tips4java.CompoundIcon
import java.awt.Component
import javax.swing.Box
import javax.swing.JLabel

/**
 * Builds the scheduler status component from a decoration value.
 *
 * It does not call [ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService],
 * so list rendering stays free of service calls.
 */
object ActionSchedulerStatusComponentService {
    fun component(status: ActionSchedulerStatus?): Component =
        status?.let {
            JLabel(
                CompoundIcon(
                    Icons.Standard.CLOCK_8x8, when (it) {
                        ActionSchedulerStatus.NOT_LAUNCHED -> Icons.Standard.CLOCK_8x8
                        ActionSchedulerStatus.ERROR -> Icons.Standard.BAN_8x8
                        ActionSchedulerStatus.SUCCESS -> Icons.Standard.CHECK_8x8
                    }
                )
            )
        } ?: Box.createHorizontalStrut(8)
}
