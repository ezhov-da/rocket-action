package ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui

import mu.KotlinLogging
import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.ActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.CreateOrUpdateActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.InvalidCronExpressionException
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerStatusUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URI
import java.time.format.DateTimeFormatter
import javax.swing.JButton
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.event.AncestorEvent
import javax.swing.event.AncestorListener
import javax.swing.event.HyperlinkEvent

private val logger = KotlinLogging.logger { }

private const val CRON_DOCUMENTATION_URL =
    "https://www.quartz-scheduler.org/documentation/quartz-2.4.x/tutorials/crontrigger.html"

private val CRON_HELP_HTML = """
    <html>
    <body style="font-family: SansSerif; font-size: 11px;">
    <h3>Scheduler time setup</h3>
    <p>The scheduler uses <b>Quartz cron expressions</b>. A schedule is defined by a string of
    <b>6</b> or <b>7</b> space-separated fields:</p>
    <pre>seconds  minutes  hours  day_of_month  month  day_of_week  [year]</pre>
    <table border="1" cellpadding="4" cellspacing="0">
      <tr><th align="left">Field</th><th align="left">Allowed values</th><th align="left">Special characters</th></tr>
      <tr><td>Seconds</td><td>0-59</td><td>, - * /</td></tr>
      <tr><td>Minutes</td><td>0-59</td><td>, - * /</td></tr>
      <tr><td>Hours</td><td>0-23</td><td>, - * /</td></tr>
      <tr><td>Day of month</td><td>1-31</td><td>, - * ? / L W</td></tr>
      <tr><td>Month</td><td>1-12 or JAN-DEC</td><td>, - * /</td></tr>
      <tr><td>Day of week</td><td>1-7 or SUN-SAT</td><td>, - * ? / L #</td></tr>
      <tr><td>Year (optional)</td><td>1970-2099</td><td>, - * /</td></tr>
    </table>
    <p><b>Special characters:</b></p>
    <ul>
      <li><b>*</b> &mdash; any value;</li>
      <li><b>?</b> &mdash; no specific value (day of month or day of week only);</li>
      <li><b>-</b> &mdash; range, for example <code>10-12</code>;</li>
      <li><b>,</b> &mdash; list, for example <code>MON,WED,FRI</code>;</li>
      <li><b>/</b> &mdash; increment, for example <code>0/15</code> &mdash; every 15 units;</li>
      <li><b>L</b> &mdash; last day of the month or last specified day of the week;</li>
      <li><b>W</b> &mdash; nearest weekday to the given day;</li>
      <li><b>#</b> &mdash; nth day of the week in the month, for example <code>6#3</code> &mdash; third Friday.</li>
    </ul>
    <p><b>Examples:</b></p>
    <table border="1" cellpadding="4" cellspacing="0">
      <tr><th align="left">Expression</th><th align="left">When to run</th></tr>
      <tr><td><code>0 0/5 * * * ?</code></td><td>every 5 minutes</td></tr>
      <tr><td><code>0 0 12 * * ?</code></td><td>every day at 12:00</td></tr>
      <tr><td><code>0 15 10 ? * MON-FRI</code></td><td>weekdays at 10:15</td></tr>
      <tr><td><code>0 0 9 1 * ?</code></td><td>the 1st day of every month at 09:00</td></tr>
      <tr><td><code>0 0 12 ? * 6L</code></td><td>the last Friday of every month at 12:00</td></tr>
      <tr><td><code>0 30 8 ? * MON#1</code></td><td>the first Monday of every month at 08:30</td></tr>
    </table>
    <p><b>Important:</b></p>
    <ul>
      <li>Do not specify values in both the "day of month" and "day of week" fields at the same time:
      one of them must contain <b>?</b>.</li>
      <li>An empty value disables automatic action execution.</li>
      <li>Time is evaluated in the application time zone.</li>
    </ul>
    <p>Full documentation:
    <a href="${CRON_DOCUMENTATION_URL}">${CRON_DOCUMENTATION_URL}</a></p>
    </body>
    </html>
""".trimIndent()

/**
 * Panel with detailed information about a scheduler of a single action.
 *
 * Contains everything that is also available in [ActionSchedulerJMenu]:
 * editing the cron expression, deleting the scheduler, status information,
 * the scheduler log and the cron help dialog.
 */
class ActionSchedulerPanel(
    private val actionId: String,
    private val actionSchedulerService: ActionSchedulerService,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val cronTextField = TextFieldWithText("cron")
    private val saveButton = JButton("Save")
    private val deleteButton = JButton("Delete")
    private val helpButton = JButton("?").apply {
        toolTipText = "Detailed instructions for scheduler time setup"
    }
    private val hintArea = JTextArea(
        "Cron: sec min hour day month day_of_week [year]\n" +
            "Example: 0 0/5 * * * ?\n" +
            "Click ? for detailed instructions"
    ).apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        isOpaque = false
        border = null
        isFocusable = false
        rows = 4
        columns = 10
        font = UIManager.getFont("Label.font") ?: font
        minimumSize = Dimension(0, 0)
    }
    private val statusLabel = JLabel("").apply { minimumSize = Dimension(0, 0) }
    private val lastLaunchLabel = JLabel("").apply { minimumSize = Dimension(0, 0) }
    private val logFile = JLabel().apply {
        isOpaque = true
        minimumSize = Dimension(0, 0)
    }
    private val logPane = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = false
        minimumSize = Dimension(0, 0)
    }

    init {
        saveButton.addActionListener {
            val cron = cronTextField.text.trim().takeIf { it.isNotEmpty() }
            try {
                actionSchedulerService.createOrUpdate(
                    CreateOrUpdateActionScheduler(
                        actionId = actionId,
                        cron = cron
                    )
                )
            } catch (e: InvalidCronExpressionException) {
                showErrorDialog(
                    title = "Invalid scheduler time configuration",
                    message = "Could not save the schedule.\n\n" +
                        "Entered value: \"${e.cron}\"\n" +
                        "Reason: ${e.reason}\n\n" +
                        "Fix the cron expression or click \"?\" to open the detailed instructions."
                )
            } catch (e: Exception) {
                logger.error(e) { "Error when saving scheduler for action '$actionId'" }
                showErrorDialog(
                    title = "Scheduler save error",
                    message = "Could not save the schedule.\n\n" +
                        "Reason: ${e.message ?: e.javaClass.simpleName}"
                )
            }
        }

        deleteButton.addActionListener {
            actionSchedulerService.delete(actionId)
        }

        helpButton.addActionListener {
            showHelpDialog()
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

        minimumSize = Dimension(0, 0)

        add(cronTextField, "width 100%, growx")
        add(saveButton)
        add(deleteButton)
        add(helpButton, "wrap")
        add(hintArea, "width 100%, growx, span 4, wrap")
        add(statusLabel, "width 100%, growx, span 4, wrap")
        add(lastLaunchLabel, "width 100%, growx, span 4, wrap")
        add(logFile, "width 100%, growx, span 4, wrap")
        add(
            JScrollPane(logPane).apply {
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED
            },
            "width 100%, height 150:200:400, grow, span 4"
        )

        val domainEventSubscriber = object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                val eventActionId = when (event) {
                    is ActionSchedulerCreatedDomainEvent -> event.actionId
                    is ActionSchedulerUpdatedDomainEvent -> event.actionId
                    is ActionSchedulerDeletedDomainEvent -> event.actionId
                    is ActionSchedulerStatusUpdatedDomainEvent -> event.actionId
                    else -> return
                }

                if (eventActionId != actionId) {
                    return
                }

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
                            statusLabel.text = ""
                            lastLaunchLabel.text = ""
                            logFile.text = ""
                            logFile.toolTipText = null
                            logFile.putClientProperty("file", null)
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

    private fun showHelpDialog() {
        val helpPane = JEditorPane("text/html", CRON_HELP_HTML).apply {
            isEditable = false
            isOpaque = false
            putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
            caretPosition = 0
            addHyperlinkListener { event ->
                if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                    event.url?.let { openDocumentation(it.toString()) }
                }
            }
        }

        val scrollPane = JScrollPane(helpPane).apply {
            preferredSize = Dimension(640, 420)
            border = null
        }

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Scheduler time setup (cron)",
            JOptionPane.INFORMATION_MESSAGE
        )
    }

    private fun openDocumentation(url: String) {
        try {
            Desktop.getDesktop().browse(URI.create(url))
        } catch (e: Exception) {
            logger.error(e) { "Can not open documentation URL '$url'" }
            showErrorDialog(
                title = "Documentation open error",
                message = "Could not open the link:\n$url\n\n" +
                    "Reason: ${e.message ?: e.javaClass.simpleName}"
            )
        }
    }

    private fun showErrorDialog(title: String, message: String) {
        val textArea = JTextArea(message, 7, 55).apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            isOpaque = false
            border = null
            font = UIManager.getFont("OptionPane.messageFont")
        }

        JOptionPane.showMessageDialog(
            this,
            textArea,
            title,
            JOptionPane.ERROR_MESSAGE
        )
    }

    private fun applyInfo(sch: ActionSchedulerDto, isUpdateCron: Boolean) {
        if (isUpdateCron) {
            cronTextField.text = sch.actionScheduler.cron.orEmpty()
        }

        statusLabel.text = "Last status: ${sch.actionScheduler.schedulerStatus}"
        lastLaunchLabel.text = "Date of the last launch: " +
            (sch.actionScheduler.dateOfTheLastLaunch
                ?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                ?: "-")

        val log = sch.logFile
        if (log == null) {
            logFile.text = ""
            logFile.toolTipText = null
            logFile.putClientProperty("file", null)
            logPane.text = ""
        } else {
            logFile.text = fileInfoToTextToText(log)
            logFile.toolTipText = "Open log file: ${log.absolutePath}"
            logFile.putClientProperty("file", log)
            logPane.text = log.readText()
            logPane.setCaretPosition(logPane.document.length)
        }
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
