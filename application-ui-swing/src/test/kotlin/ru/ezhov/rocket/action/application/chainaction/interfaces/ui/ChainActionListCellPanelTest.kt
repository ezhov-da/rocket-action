package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class ChainActionListCellPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");
        frame.add(
            ChainActionListCellPanel(
                chainAction = ChainAction(
                    id = "123",
                    name = "Test name",
                    description = "Test description",
                    actions = listOf(
                        ActionOrder(
                            "000", "11"
                        ),
                        ActionOrder(
                            "111", "22"
                        )
                    )
                ),
                backgroundColor = null,
                firstAtomicAction = mockk {
                    every { contractType } returns ContractType.IN_OUT
                }
            )
        )
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
