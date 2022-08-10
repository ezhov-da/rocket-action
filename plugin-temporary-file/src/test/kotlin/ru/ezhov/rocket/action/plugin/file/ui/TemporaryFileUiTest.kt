package ru.ezhov.rocket.action.plugin.file.ui

import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import javax.swing.JFrame
import javax.swing.SwingUtilities

object TemporaryFileUiTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            JFrame().apply {
                add(TemporaryFileUi(TemporaryFileService()))

                pack()
                isVisible = true
            }
        }
    }
}
