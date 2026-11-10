package ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionRunner
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionService
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.event.KeyEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.KeyStroke

/**
 * Window with the ordered list of actions executed at application start.
 */
class StartupActionsFrame(
    startupActionService: StartupActionService,
    startupActionRunner: StartupActionRunner,
    startupActionLogRepository: StartupActionLogRepository,
) : JFrame("Startup actions") {
    init {
        iconImage = Icons.Standard.POWER_STANDBY_16x16.toImage()
        contentPane = StartupActionsPanel(
            startupActionService = startupActionService,
            runner = startupActionRunner,
            logRepository = startupActionLogRepository,
        )
        defaultCloseOperation = DISPOSE_ON_CLOSE
        size = SizeUtil.dimension(0.75, 0.65)

        rootPane.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW,
        )
    }
}
