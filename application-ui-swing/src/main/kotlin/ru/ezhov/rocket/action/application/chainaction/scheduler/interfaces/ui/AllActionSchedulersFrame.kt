package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.KeyStroke

/**
 * Window with a list of all action schedulers.
 */
class AllActionSchedulersFrame(
    actionSchedulerService: ActionSchedulerService,
) : JFrame("Schedulers") {
    init {
        val schedulersPanel = AllActionSchedulersPanel(actionSchedulerService)

        iconImage = Icons.Standard.CLOCK_16x16.toImage()
        contentPane = schedulersPanel
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = SizeUtil.dimension(0.75, 0.65)

        rootPane.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW,
        )
    }
}
