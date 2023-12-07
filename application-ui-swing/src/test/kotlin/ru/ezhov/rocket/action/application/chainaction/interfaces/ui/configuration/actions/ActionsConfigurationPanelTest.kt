package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class ActionsConfigurationPanelTest

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");
        frame.add(
            ActionsConfigurationPanel(
                atomicActionService = AtomicActionService(JsonAtomicActionRepository(TestUtilsFactory.objectMapper)),
                chainActionService = mockk(),
                createAndEditChainActionDialog = mockk(),
                actionExecutor = mockk(),
            )
        )
        frame.setSize(1000, 700)
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
