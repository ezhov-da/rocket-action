package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonAtomicActionRepository
import ru.ezhov.rocket.action.application.chainaction.infrastructure.JsonChainActionRepository
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class EditChainActionDialogTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }

        val dialog = EditChainActionDialog(
            actionExecutorService = mockk(),
            chainActionService = ChainActionService(JsonChainActionRepository(TestUtilsFactory.objectMapper)),
            atomicActionService = AtomicActionService(JsonAtomicActionRepository(TestUtilsFactory.objectMapper)),
        ).showEditDialog(
            ChainAction(
                id = "123",
                name = "Test",
                description = "Test description",
                actions = emptyList()
            )
        )
    }
}
