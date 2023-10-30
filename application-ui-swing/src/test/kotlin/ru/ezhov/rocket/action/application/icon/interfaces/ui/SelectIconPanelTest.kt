package ru.ezhov.rocket.action.application.icon.interfaces.ui

import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class SelectIconPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")

        frame.add(SelectIconPanel())
        frame.setSize(300, 200)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isAlwaysOnTop = true
        frame.isVisible = true
    }
}
