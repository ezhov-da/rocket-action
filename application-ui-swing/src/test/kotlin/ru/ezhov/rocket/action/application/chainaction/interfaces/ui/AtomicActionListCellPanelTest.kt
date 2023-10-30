package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class AtomicActionListCellPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");
        frame.add(
            AtomicActionListCellPanel(
                atomicAction = AtomicAction(
                    id = "123",
                    name = "Test name",
                    description = "Test description",
                    contractType = ContractType.IN_OUT,
                    engine = AtomicActionEngine.KOTLIN,
                    source = AtomicActionSource.FILE,
                    data = "Text",
                )
            )
        )
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
