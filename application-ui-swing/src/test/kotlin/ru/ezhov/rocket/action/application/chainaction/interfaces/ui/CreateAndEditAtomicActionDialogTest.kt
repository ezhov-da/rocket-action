package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.infrastructure.InMemoryAtomicActionRepository

internal class CreateAndEditAtomicActionDialogTest

fun main(args: Array<String>) {
    create()
}

private fun create() {
    val dialog = CreateAndEditAtomicActionDialog(
        AtomicActionService(
            InMemoryAtomicActionRepository(),
        )
    )
    dialog.showCreateDialog()
    System.exit(0)
}

private fun edit() {
    val dialog = CreateAndEditAtomicActionDialog(
        mockk()

    )
    dialog.isVisible = true
    System.exit(0)
}
