package ru.ezhov.rocket.action.plugin.jira.worklog

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.RawTextValidator
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations.ValidationRule
import ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure.JiraCommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure.JiraCommitTimeTaskInfoRepository
import ru.ezhov.rocket.action.plugin.jira.worklog.ui.JiraWorkLogUIFrame
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import java.awt.Component
import java.io.File
import java.net.URI
import java.util.*
import javax.swing.Icon
import javax.swing.JMenuItem

private val logger = KotlinLogging.logger {}

class JiraWorklogRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null
    private var jiraWorkLogUIFrame: JiraWorkLogUIFrame? = null


    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() }
            ?.let { label ->
                val description = settings.settings()[DESCRIPTION].orEmpty()

                val username = settings.settings()[USERNAME].orEmpty()
                val password = settings.settings()[PASSWORD].orEmpty()
                val url = try {
                    URI.create(settings.settings()[URL].orEmpty())
                } catch (ex: Exception) {
                    val text = "Invalid URL for Jira work log plugin"
                    logger.warn(ex) { text }
                    actionContext!!.notification().show(type = NotificationType.WARN, text = text)
                    return null
                }
                val tasks = settings.settings()[PREDEFINED_TASKS]
                    ?.split("\n")
                    ?.mapNotNull { r ->
                        r
                            .split("___")
                            .takeIf { it.size == 2 }
                            ?.let { Task(id = it[0], name = it[1]) }
                    }
                    .orEmpty()

                val aliasForTaskIds = AliasForTaskIds.of(settings.settings()[ALIAS_FOR_TASK_IDS])

                val menuItem = JMenuItem(label)
                menuItem.icon = actionContext!!.icon().by(AppIcon.CLOCK)
                menuItem.toolTipText = description

                jiraWorkLogUIFrame =
                    JiraWorkLogUIFrame(
                        tasks = tasks,
                        commitTimeService = JiraCommitTimeService(username = username, password = password, url = url),
                        commitTimeTaskInfoRepository = JiraCommitTimeTaskInfoRepository(
                            username = username,
                            password = password,
                            url = url
                        ),
                        delimiter = settings.settings()[DELIMITER_TASK_INFO] ?: DEFAULT_DELIMITER_TASK_INFO,
                        dateFormatPattern = "yyyyMMddHHmm",
                        constantsNowDate =
                        (settings.settings()[CONSTANTS_NOW_DATE]
                            ?.takeIf { it.isNotBlank() }
                            ?: DEFAULT_CONSTANTS_NOW_DATE)
                            .let {
                                it
                                    .split(",")
                                    .map { it -> it.trim() }
                            },
                        aliasForTaskIds = aliasForTaskIds,
                        linkToWorkLog =
                        settings.settings()[LINK_TO_WORK_LOG]
                            ?.takeIf { it.isNotBlank() }
                            ?.let { link ->
                                try {
                                    URI.create(link)
                                } catch (ex: Exception) {
                                    val msg = "Error link $link"
                                    logger.warn(ex) { msg }
                                    actionContext!!.notification().show(NotificationType.WARN, msg)
                                    null
                                }
                            },
                        fileForSave = File(settings.settings()[FILE_PATH_WORK_LOG] ?: defaultFile()),
                        context = context,
                        validator = RawTextValidator(
                            settings.settings()[TEXT_VALIDATIONS] ?: RawTextValidator.EMPTY_RULES
                        ),
                        maxTimeInMinutes = settings.settings()[MAX_TIME_IN_MINUTES]?.toIntOrNull()
                    )

                menuItem.addActionListener {
                    //https://stackoverflow.com/questions/4005491/how-to-activate-window-in-java
                    jiraWorkLogUIFrame!!.showToFront()
                }

                context.search().register(settings.id(), label)
                context.search().register(settings.id(), description)

                object : RocketAction, RocketActionHandlerFactory {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menuItem
                    override fun handler(): RocketActionHandler = object : RocketActionHandler {
                        override fun id(): String = settings.id()

                        override fun contracts(): List<RocketActionHandlerCommandContract> =
                            listOf(
                                object : RocketActionHandlerCommandContract {
                                    override fun commandName(): String = "append-text-to-end-and-save"

                                    override fun title(): String = label

                                    override fun description(): String =
                                        "Hours worked report. Adding text to the end and saving the file"

                                    override fun inputArguments(): List<RocketActionHandlerProperty> =
                                        listOf(object : RocketActionHandlerProperty {
                                            override fun key(): RocketActionHandlerPropertyKey =
                                                RocketActionHandlerPropertyKey("text")

                                            override fun name(): String = "Text"

                                            override fun description(): String = "Text to add"

                                            override fun isRequired(): Boolean = true

                                            override fun property(): RocketActionHandlerPropertySpec =
                                                RocketActionHandlerPropertySpec.StringPropertySpec()

                                        })

                                    override fun outputParams(): List<RocketActionHandlerProperty> = emptyList()

                                }
                            )

                        override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
                            if (command.commandName == "append-text-to-end-and-save") {
                                command.arguments["text"]?.let { text ->
                                    jiraWorkLogUIFrame!!.appendTextToCurrentAndSave(text)
                                }
                            }
                            return RocketActionHandleStatus.Success()
                        }
                    }
                }
            }

    override fun type(): RocketActionType = RocketActionType { "JIRA_WORK_LOG" }

    override fun description(): String = "Hours tracking in Jira"

    override fun asString(): List<String> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Title",
                description = "Title to be displayed",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Description",
                description = "A description that will pop up on hover, otherwise the path will be displayed",
                required = false
            ),
            createRocketActionProperty(
                key = URL,
                name = "Jira URL",
                description = "Jira URL",
                required = true
            ),
            createRocketActionProperty(
                key = USERNAME,
                name = "Username",
                description = "Username",
                required = true
            ),
            createRocketActionProperty(
                key = PASSWORD,
                name = "Password",
                description = "Password",
                required = true
            ),
            createRocketActionProperty(
                key = PREDEFINED_TASKS,
                name = "Preset tasks for quick selection",
                description = "Format: Task ID___Title",
                required = false
            ),
            createRocketActionProperty(
                key = DELIMITER_TASK_INFO,
                name = "Task information delimiter",
                description = "This delimiter will be used to enter data into the text field",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_DELIMITER_TASK_INFO)
            ),
            createRocketActionProperty(
                key = CONSTANTS_NOW_DATE,
                name = "Constants to indicate the current time",
                description = "Through a comma. Used to replace when entering data for the current time. Example: now,n",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_CONSTANTS_NOW_DATE)
            ),
            createRocketActionProperty(
                key = ALIAS_FOR_TASK_IDS,
                name = "Aliases for task IDs",
                description = """Used to replace the alias with the task ID.
                    |Example:
                    |TASK-day,org
                    |TASK-15_col,d""".trimMargin(),
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_CONSTANTS_NOW_DATE)
            ),
            createRocketActionProperty(
                key = LINK_TO_WORK_LOG,
                name = "Link to time tracking page",
                description = "Link to the time tracking page for a quick transition",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec()
            ),
            createRocketActionProperty(
                key = FILE_PATH_WORK_LOG,
                name = "Path to the file to save the report on the write-off of time",
                description = "Path to the file to save the report on the write-off of time",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(
                    defaultValue = defaultFile()
                )
            ),
            createRocketActionProperty(
                key = TEXT_VALIDATIONS,
                name = "Possible validations for text",
                description = "Available validations:\n" +
                    ValidationRule.values().joinToString(separator = "\n") { "${it.name}:${it.description}" } +
                    "\nEnter settings in lines, for example: '${ValidationRule.MIN_LENGTH} 12' this means, " +
                    "that the minimum text length is 12",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec()
            ),
            createRocketActionProperty(
                key = MAX_TIME_IN_MINUTES,
                name = "Maximum time in minutes",
                description = "Used to calculate the remaining time for making",
                required = false,
                property = RocketActionPropertySpec.IntPropertySpec()
            ),
        )
    }

    private fun defaultFile() = "./.jira-plugin/worklog/worklog-${UUID.randomUUID()}.txt"

    override fun name(): String = "Hours tracking in Jira"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.CLOCK)

    companion object {
        private const val LABEL = "label"
        private const val DESCRIPTION = "description"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"
        private const val URL = "url"

        private const val PREDEFINED_TASKS = "predefinedTasks"
        private const val DELIMITER_TASK_INFO = "delimiterTaskInfo"
        private const val DEFAULT_DELIMITER_TASK_INFO = "_"
        private const val CONSTANTS_NOW_DATE = "constantsNowDate"
        private const val DEFAULT_CONSTANTS_NOW_DATE = "now"

        private const val ALIAS_FOR_TASK_IDS = "aliasForTaskIds"
        private const val LINK_TO_WORK_LOG = "linkToWorkLog"
        private const val FILE_PATH_WORK_LOG = "filePathWorkLog"
        private const val TEXT_VALIDATIONS = "textValidations"
        private const val MAX_TIME_IN_MINUTES = "maxTimeInMinutes"
    }
}
