package ru.ezhov.rocket.action.plugin.jira.worklog.ui

import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.CommitTimeTaskInfoRepository
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.Validator
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.io.File
import java.net.URI
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JToolBar

class JiraWorkLogUIFrame(
    tasks: List<Task> = emptyList(),
    commitTimeService: CommitTimeService,
    commitTimeTaskInfoRepository: CommitTimeTaskInfoRepository,
    context: RocketActionContext,
    validator: Validator,
    delimiter: String,
    dateFormatPattern: String,
    constantsNowDate: List<String>,
    aliasForTaskIds: AliasForTaskIds,
    linkToWorkLog: URI? = null,
    fileForSave: File,
    maxTimeInMinutes: Int?,
) : JFrame("Hours tracking in Jira") {
    private val commitTimePanel = CommitTimePanel(
        tasks = tasks,
        commitTimeService = commitTimeService,
        delimiter = delimiter,
        dateFormatPattern = dateFormatPattern,
        constantsNowDate = constantsNowDate,
        aliasForTaskIds = aliasForTaskIds,
        linkToWorkLog = linkToWorkLog,
        fileForSave = fileForSave,
        commitTimeTaskInfoRepository = commitTimeTaskInfoRepository,
        context = context,
        validator = validator,
        maxTimeInMinutes = maxTimeInMinutes,
    )
    private val dimension = calculateSize()

    init {
        val toolBar = JToolBar().apply {
            add(
                JButton("Download text")
                    .apply {
                        addActionListener {
                            commitTimePanel.loadText()
                        }
                    }
            )
        }
        iconImage = context.icon().by(AppIcon.ROCKET_APP).toImage()

        add(toolBar, BorderLayout.NORTH)
        add(commitTimePanel, BorderLayout.CENTER)

        size = dimension
        setLocationRelativeTo(null)
        defaultCloseOperation = DISPOSE_ON_CLOSE
        maximumSize = dimension
        preferredSize = dimension
    }

    fun appendTextToCurrentAndSave(text: String) {
        commitTimePanel.appendTextToCurrentAndSave(text)
    }

    private fun calculateSize() = Toolkit.getDefaultToolkit().screenSize.let {
        Dimension(
            (it.width * 0.4).toInt(),
            (it.height * 0.5).toInt()
        )
    }
}
