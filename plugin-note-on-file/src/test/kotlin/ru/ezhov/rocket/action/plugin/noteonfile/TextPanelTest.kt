package ru.ezhov.rocket.action.plugin.noteonfile

import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        JFrame()
            .apply {
                add(
                    TextPanel(
                        path = "", label = "Test", loadOnInitialize = true,
                        style = null, addStyleSelected = true
                    )
                )

                pack()
                defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                isVisible = true
            }
    }
}