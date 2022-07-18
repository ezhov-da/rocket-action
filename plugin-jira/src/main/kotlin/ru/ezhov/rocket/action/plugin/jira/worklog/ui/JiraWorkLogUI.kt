package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.net.URI
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class JiraWorkLogUI(
    private val tasks: List<Task> = emptyList(),
    private val commitTimeService: CommitTimeService,
    delimiter: String,
    dateFormatPattern: String,
    constantsNowDate: List<String>,
    aliasForTaskIds: AliasForTaskIds,
    linkToWorkLog: URI? = null,
) : JPanel() {
    private val commitTimePanel = CommitTimePanel(
        tasks = tasks,
        commitTimeService = commitTimeService,
        delimiter = delimiter,
        dateFormatPattern = dateFormatPattern,
        constantsNowDate = constantsNowDate,
        aliasForTaskIds = aliasForTaskIds,
        linkToWorkLog = linkToWorkLog,
    )
    private val dimension = calculateSize()

    init {
        val toolBar = JToolBar()
        toolBar.add(
            JButton("Открыть в отдельном окне")
                .apply {
                    addActionListener {
                        SwingUtilities.invokeLater {
                            val frame = JFrame("Внесение времени в Jira")
                            frame.add(
                                JiraWorkLogUI(
                                    tasks = tasks,
                                    commitTimeService = commitTimeService,
                                    delimiter = delimiter,
                                    dateFormatPattern = dateFormatPattern,
                                    constantsNowDate = constantsNowDate,
                                    aliasForTaskIds = aliasForTaskIds,
                                    linkToWorkLog = linkToWorkLog,
                                ),
                                BorderLayout.CENTER
                            )

                            frame.size = dimension
                            frame.setLocationRelativeTo(null)
                            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                            frame.isVisible = true
                        }
                    }
                }
        )

        layout = BorderLayout()
        add(toolBar, BorderLayout.NORTH)
        add(commitTimePanel, BorderLayout.CENTER)
        maximumSize = dimension
        preferredSize = dimension
    }

    private fun calculateSize() = Toolkit.getDefaultToolkit().screenSize.let {
        Dimension(
            (it.width * 0.4).toInt(),
            (it.height * 0.5).toInt()
        )
    }
}
