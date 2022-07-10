package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.JPanel

class JiraWorkLogUI(
    private val tasks: List<Task> = emptyList(),
    private val commitTimeService: CommitTimeService,
) : JPanel() {
    private val commitTimePanel = CommitTimePanel(tasks = tasks, commitTimeService = commitTimeService)

    init {
        layout = BorderLayout()
        add(commitTimePanel, BorderLayout.CENTER)
        val dimensionScreen = Toolkit.getDefaultToolkit().screenSize
        val dimension = Dimension(
            (dimensionScreen.width * 0.4).toInt(),
            (dimensionScreen.height * 0.3).toInt()
        )
        maximumSize = dimension
        preferredSize = dimension
    }
}
