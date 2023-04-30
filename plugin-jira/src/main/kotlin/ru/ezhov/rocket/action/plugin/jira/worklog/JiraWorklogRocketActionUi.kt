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
import ru.ezhov.rocket.action.plugin.jira.worklog.ui.JiraWorkLogUI
import java.awt.Component
import java.io.File
import java.net.URI
import java.util.*
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class JiraWorklogRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

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

                val menu = JMenu(label)
                menu.icon = actionContext!!.icon().by(AppIcon.CLOCK)
                menu.toolTipText = description
                val jiraWorkLogUI =
                    JiraWorkLogUI(
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
                menu.add(jiraWorkLogUI)

                context.search().register(settings.id(), label)
                context.search().register(settings.id(), description)

                object : RocketAction, RocketActionHandlerFactory {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                    override fun handler(): RocketActionHandler = object : RocketActionHandler {
                        override fun id(): String = settings.id()

                        override fun contracts(): List<RocketActionHandlerCommandContract> =
                            listOf(
                                object : RocketActionHandlerCommandContract {
                                    override fun commandName(): String = "append-text-to-end-and-save"

                                    override fun title(): String = label

                                    override fun description(): String = """Отчёт по отработанным часам.
                                        |Добавление текста в конец и сохранение файла""".trimMargin()

                                    override fun inputArguments(): List<RocketActionHandlerProperty> =
                                        listOf(object : RocketActionHandlerProperty {
                                            override fun key(): RocketActionHandlerPropertyKey =
                                                RocketActionHandlerPropertyKey("text")

                                            override fun name(): String = "Текст"

                                            override fun description(): String = "Текст для добавления"

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
                                    jiraWorkLogUI.appendTextToCurrentAndSave(text)
                                }
                            }
                            return RocketActionHandleStatus.Success()
                        }
                    }
                }
            }

    override fun type(): RocketActionType = RocketActionType { "JIRA_WORK_LOG" }

    override fun description(): String = "Внесение отработанного времени в Jira"

    override fun asString(): List<String> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = LABEL,
                name = "Заголовок",
                description = "Заголовок, который будет отображаться",
                required = true
            ),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = "Описание",
                description = """Описание, которое будет всплывать при наведении,
                            |в случае отсутствия будет отображаться путь""".trimMargin(),
                required = false
            ),
            createRocketActionProperty(
                key = URL,
                name = "URL для Jira",
                description = "URL для Jira",
                required = true
            ),
            createRocketActionProperty(
                key = USERNAME,
                name = "Имя пользователя",
                description = "Имя пользователя",
                required = true
            ),
            createRocketActionProperty(
                key = PASSWORD,
                name = "Пароль",
                description = "Пароль",
                required = true
            ),
            createRocketActionProperty(
                key = PREDEFINED_TASKS,
                name = "Предустановленные задачи для быстрого выбора",
                description = "Формат: ID задачи___Название",
                required = false
            ),
            createRocketActionProperty(
                key = DELIMITER_TASK_INFO,
                name = "Разделитель информации о задаче",
                description = "Этот разделитель будет использоваться для внесения данных в текстовое поле",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_DELIMITER_TASK_INFO)
            ),
            createRocketActionProperty(
                key = CONSTANTS_NOW_DATE,
                name = "Константы, для обозначения текущего времени",
                description = "Через запятую. Используется для замены при внесении " +
                    "данных на текущее время. Пример: now,n,сейчас",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_CONSTANTS_NOW_DATE)
            ),
            createRocketActionProperty(
                key = ALIAS_FOR_TASK_IDS,
                name = "Псевдонимы для идентификаторов задач",
                description = """Используется для замены псевдонима на ID задачи.
                    |Пример:
                    |TASK-12_внеп,проп
                    |TASK-15_что,col""".trimMargin(),
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec(defaultValue = DEFAULT_CONSTANTS_NOW_DATE)
            ),
            createRocketActionProperty(
                key = LINK_TO_WORK_LOG,
                name = "Ссылка на страницу учёта времени",
                description = "Ссылка на страницу учёта времени для быстрого перехода",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec()
            ),
            createRocketActionProperty(
                key = FILE_PATH_WORK_LOG,
                name = "Путь к файлу сохранения отчёта по списанию времени",
                description = "Путь к файлу сохранения отчёта по списанию времени",
                required = true,
                property = RocketActionPropertySpec.StringPropertySpec(
                    defaultValue = defaultFile()
                )
            ),
            createRocketActionProperty(
                key = TEXT_VALIDATIONS,
                name = "Возможные валидации для текста",
                description = "Доступные валидации:\n" +
                    ValidationRule.values().joinToString(separator = "\n") { "${it.name}:${it.description}" } +
                    "\nВведите настройки в строках, например: '${ValidationRule.MIN_LENGTH} 12' это значит, " +
                    "что минимальная длина текста - 12",
                required = false,
                property = RocketActionPropertySpec.StringPropertySpec()
            ),
            createRocketActionProperty(
                key = MAX_TIME_IN_MINUTES,
                name = "Максимальное время в минутах",
                description = "Используется для вычисления оставшегося времени для внесения",
                required = false,
                property = RocketActionPropertySpec.IntPropertySpec()
            ),
        )
    }

    private fun defaultFile() = "./.jira-plugin/worklog/worklog-${UUID.randomUUID()}.txt"

    override fun name(): String = "Внесение отработанного времени в Jira"

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
