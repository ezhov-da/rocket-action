package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.ActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.CreateOrUpdateActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerStatusUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener

class ActionSchedulerJMenu(
    actionId: String,
    actionSchedulerService: ActionSchedulerService
) : JMenu("Scheduler") {
    init {
        add(SchedulerPanel(actionId, actionSchedulerService))
    }

    private class SchedulerPanel(
        private val actionId: String,
        private val actionSchedulerService: ActionSchedulerService
    ) : JPanel(MigLayout(/*"debug"*/)) {
        private val cronTextField = TextFieldWithText("cron")
        private val saveButton = JButton("Save")
        private val deleteButton = JButton("Delete")
        private val labelInfo = JLabel("")
        private val logFile = JLabel().apply { isOpaque = true }
        private val logPane = JEditorPane()

        init {
            saveButton.addActionListener {
                actionSchedulerService.createOrUpdate(
                    CreateOrUpdateActionScheduler(
                        actionId = actionId,
                        cron = cronTextField.text.takeIf { it.isNotBlank() }
                    )
                )
            }

            deleteButton.addActionListener {
                actionSchedulerService.delete(actionId)
            }

            actionSchedulerService.scheduler(actionId)?.let { sch ->
                applyInfo(sch, true)
            }


            logFile.addMouseListener(object : MouseAdapter() {
                private var defaultTextColor = logFile.foreground
                override fun mouseEntered(e: MouseEvent?) {
                    if (logFile.text.isNotBlank()) {
                        defaultTextColor = foreground
                        logFile.foreground = Color.BLUE
                        logFile.cursor = Cursor(Cursor.HAND_CURSOR)
                    }
                }

                override fun mouseExited(e: MouseEvent?) {
                    logFile.foreground = defaultTextColor
                    logFile.cursor = Cursor.getDefaultCursor()
                }

                override fun mouseReleased(e: MouseEvent) {
                    (logFile.getClientProperty("file") as? File)?.let { file ->
                        SwingUtilities.invokeLater {
                            Desktop.getDesktop().open(file)
                        }
                    }
                }
            })

            add(cronTextField, "width 100%, grow")
            add(saveButton)
            add(deleteButton, "wrap")
            add(labelInfo, "width 100%, grow, span 3, wrap")
            add(logFile, "width 100%, grow, span 3, wrap")
            add(JScrollPane(logPane), "width 100%, height 100%, grow, span 3")

            SizeUtil.setAllSize(this, SizeUtil.dimension(0.3, 0.3))

            val domainEventSubscriber = object : DomainEventSubscriber {
                override fun handleEvent(event: DomainEvent) {
                    when (event) {
                        is ActionSchedulerCreatedDomainEvent -> {
                            SwingUtilities.invokeLater {
                                actionSchedulerService.scheduler(actionId)?.let { sch ->
                                    applyInfo(sch, true)
                                }
                            }
                        }

                        is ActionSchedulerUpdatedDomainEvent -> {
                            SwingUtilities.invokeLater {
                                actionSchedulerService.scheduler(actionId)?.let { sch ->
                                    applyInfo(sch, false)
                                }
                            }
                        }

                        is ActionSchedulerDeletedDomainEvent -> {
                            SwingUtilities.invokeLater {
                                cronTextField.text = ""
                                logPane.text = ""
                                labelInfo.text = ""
                            }
                        }

                        is ActionSchedulerStatusUpdatedDomainEvent -> {
                            SwingUtilities.invokeLater {
                                actionSchedulerService.scheduler(actionId)?.let { sch ->
                                    applyInfo(sch, false)
                                }
                            }
                        }
                    }
                }

                override fun subscribedToEventType(): List<Class<*>> =
                    listOf(
                        ActionSchedulerCreatedDomainEvent::class.java,
                        ActionSchedulerUpdatedDomainEvent::class.java,
                        ActionSchedulerDeletedDomainEvent::class.java,
                        ActionSchedulerStatusUpdatedDomainEvent::class.java,
                    )
            }

            DomainEventFactory.subscriberRegistrar.subscribe(domainEventSubscriber)

            addAncestorListener(object : AncestorListener {
                override fun ancestorAdded(event: AncestorEvent) {
                }

                // HIDDEN MENU
                override fun ancestorRemoved(event: AncestorEvent) {
                    DomainEventFactory.subscriberRegistrar.unsubscribe(domainEventSubscriber)
                }

                override fun ancestorMoved(event: AncestorEvent) {
                }
            })
        }

        private fun applyInfo(sch: ActionSchedulerDto, isUpdateCron: Boolean) {
            if (isUpdateCron) {
                cronTextField.text = sch.actionScheduler.cron.orEmpty()
            }
            labelInfo.text = buildStatusLabelText(sch)
            logPane.text = sch.logFile
                ?.let { f ->
                    logFile.text = fileInfoToTextToText(f)
                    logFile.putClientProperty("file", f)
                    f.readText()
                        .apply {
                            logPane.setCaretPosition(logPane.document.length)
                        }
                }
                .orEmpty()
        }

        private fun buildStatusLabelText(actionScheduler: ActionSchedulerDto): String {
            return "Last status: ${actionScheduler.actionScheduler.schedulerStatus}. " +
                "Date of the last launch: ${actionScheduler.actionScheduler.dateOfTheLastLaunch?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "-"}"
        }

        fun fileInfoToTextToText(file: File): String {
            val size = when {
                file.length() > 1024.0 * 1024.0 -> FileSize(
                    (file.length() / 1024.0 / 1024.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP),
                    FileSizeType.MB
                )

                else -> FileSize(
                    (file.length() / 1024.0).toBigDecimal().setScale(2, RoundingMode.HALF_UP),
                    FileSizeType.KB
                )
            }

            val sizeText = if (size.type == FileSizeType.MB && size.size.toDouble() > 1024.0 * 1024.0 * 5) {
                "Size: <span style=\"color:red;\">${size.size.toDouble()} ${size.type}</span>"
            } else {
                "Size: ${size.size.toDouble()} ${size.type}"
            }

            val nameText = "Name: ${file.name}"

            return "<html>${sizeText}. ${nameText}"
        }
    }

    private data class FileSize(
        val size: BigDecimal,
        val type: FileSizeType,
    )

    private enum class FileSizeType {
        MB,
        KB,
        ;
    }
}


