package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class FavoriteTasksPanelTest

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")

        val panel = FavoriteTasksPanel(
            listOf(
                Task(
                    "123",
                    "Test"
                ),
                Task(
                    "456",
                    "Test"
                )
            ),
            AliasForTaskIds.EMPTY
        ) { task -> println(task.id) }

        frame.add(panel)
        frame.setSize(1000, 200)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}
