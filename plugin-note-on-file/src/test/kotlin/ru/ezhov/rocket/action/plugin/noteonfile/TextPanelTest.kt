package ru.ezhov.rocket.action.plugin.noteonfile

import java.nio.file.Files
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.io.path.absolutePathString

fun main() {
    SwingUtilities.invokeLater {
        JFrame()
            .apply {
                add(
                    TextPanel(
                        path = Files.createTempFile("123", "111").absolutePathString(),
                        label = "Test",
                        loadOnInitialize = true,
                        style = null,
                        addStyleSelected = true,
                        delimiter = "",
                        textAutoSave = TextAutoSave(enable = true, delayInSeconds = 3),
                    )
                )

                pack()
                defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                isVisible = true
            }
    }
}
