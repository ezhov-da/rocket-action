package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryAtomicActionRepository

fun main(args: Array<String>) {
    val dialog = CreateAtomicActionDialog(
        AtomicActionService(
            InMemoryAtomicActionRepository(),
        )
    )
    dialog.isVisible = true
    System.exit(0)
}

internal class CreateAtomicActionDialogTest
