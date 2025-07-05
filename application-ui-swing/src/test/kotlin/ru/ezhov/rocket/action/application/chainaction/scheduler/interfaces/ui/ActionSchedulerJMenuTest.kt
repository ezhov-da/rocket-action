package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import io.mockk.mockk
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.SwingUtilities

class ActionSchedulerJMenuTest

fun main() {
    SwingUtilities.invokeLater {
        val frame = JFrame()
        frame.add(JMenuBar().apply { add(ActionSchedulerJMenu("", mockk())) }, BorderLayout.NORTH)
        frame.setSize(300, 300)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}
