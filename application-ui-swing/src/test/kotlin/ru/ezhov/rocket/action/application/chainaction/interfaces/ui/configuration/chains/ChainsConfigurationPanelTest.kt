package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains

import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonChainActionRepository
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class ChainsConfigurationPanelTest

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");
        frame.add(
            ChainsConfigurationPanel(
                actionExecutorService = mockk(),
                chainActionService = ChainActionService(JsonChainActionRepository(TestUtilsFactory.objectMapper)),
                atomicActionService = AtomicActionService(JsonAtomicActionRepository(TestUtilsFactory.objectMapper)),
                mockk(),
            )
        )
        frame.setSize(1000, 700)
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
