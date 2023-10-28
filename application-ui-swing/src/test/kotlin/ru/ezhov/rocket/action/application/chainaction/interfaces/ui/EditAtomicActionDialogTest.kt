package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import io.mockk.mockk

internal class EditAtomicActionDialogTest

fun main(args: Array<String>) {
    val dialog = EditAtomicActionDialog(
        mockk()

    )
    dialog.isVisible = true
    System.exit(0)
}
