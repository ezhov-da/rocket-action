package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import arrow.core.Either
import arrow.core.right
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeServiceException
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.CommitTimeTask
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________")
        frame.add(
            CommitTimePanel(
                tasks = listOf(
                    Task(
                        id = "123",
                        name = "ляляля",
                    ),
                    Task(
                        id = "234",
                        name = "432",
                    ),
                ),
                commitTimeService = object : CommitTimeService {
                    override fun commit(tasks: List<CommitTimeTask>): Either<CommitTimeServiceException, Unit> {
                        println("commit")
                        return Unit.right()
                    }
                }
            )
        )

        frame.setSize(1000, 600)
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}
