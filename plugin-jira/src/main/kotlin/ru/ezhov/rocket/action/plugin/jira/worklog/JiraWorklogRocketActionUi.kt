package ru.ezhov.rocket.action.plugin.jira.worklog

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.AliasForTaskIds
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure.JiraCommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.ui.JiraWorkLogUI
import java.awt.Component
import java.io.File
import java.net.URI
import java.util.UUID
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger {}

class JiraWorklogRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val icon = IconRepositoryFactory.repository.by(AppIcon.CLOCK)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
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
                    NotificationFactory.notification.show(type = NotificationType.WARN, text = text)
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
                menu.icon = icon
                menu.toolTipText = description
                menu.add(
                    JiraWorkLogUI(
                        tasks = tasks,
                        commitTimeService = JiraCommitTimeService(username = username, password = password, url = url),
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
                                    NotificationFactory.notification.show(NotificationType.WARN, msg)
                                    null
                                }
                            },
                        fileForSave = File(settings.settings()[FILE_PATH_WORK_LOG] ?: defaultFile())
                    ),
                )

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(description.contains(search, ignoreCase = true))

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                }
            }

    override fun type(): RocketActionType = RocketActionType { "JIRA_WORK_LOG" }

    override fun description(): String = "Внесение отработанного времени в Jira"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

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
        )
    }

    private fun defaultFile() = "./.jira-plugin/worklog/worklog-${UUID.randomUUID()}.txt"

    override fun name(): String = "Внесение отработанного времени в Jira"

    override fun icon(): Icon? = icon

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val USERNAME = RocketActionConfigurationPropertyKey("username")
        private val PASSWORD = RocketActionConfigurationPropertyKey("password")
        private val URL = RocketActionConfigurationPropertyKey("url")

        private val PREDEFINED_TASKS = RocketActionConfigurationPropertyKey("predefinedTasks")
        private val DELIMITER_TASK_INFO = RocketActionConfigurationPropertyKey("delimiterTaskInfo")
        private const val DEFAULT_DELIMITER_TASK_INFO = "_"
        private val CONSTANTS_NOW_DATE = RocketActionConfigurationPropertyKey("constantsNowDate")
        private const val DEFAULT_CONSTANTS_NOW_DATE = "now"

        private val ALIAS_FOR_TASK_IDS = RocketActionConfigurationPropertyKey("aliasForTaskIds")
        private val LINK_TO_WORK_LOG = RocketActionConfigurationPropertyKey("linkToWorkLog")
        private val FILE_PATH_WORK_LOG = RocketActionConfigurationPropertyKey("filePathWorkLog")
    }
}
