package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class IconSelectionPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")

        frame.add(IconSelectionPanel(IconRepository()))

        frame.pack()
//        frame.setSize(400, 400)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isAlwaysOnTop = true
        frame.isVisible = true
    }
}
