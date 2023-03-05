package ru.ezhov.rocket.action.application.variables.interfaces.ui

import javax.swing.SwingUtilities


fun main() {
    SwingUtilities.invokeLater {
        val frame = VariablesFrame()

        frame.isVisible = true
    }
}

// для обнаружения тестового класса
internal class VariablesFrameTest
