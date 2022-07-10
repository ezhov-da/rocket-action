package ru.ezhov.rocket.action.plugin.jira.worklog

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.plugin.jira.worklog.domain.model.Task
import ru.ezhov.rocket.action.plugin.jira.worklog.infrastructure.JiraCommitTimeService
import ru.ezhov.rocket.action.plugin.jira.worklog.ui.JiraWorkLogUI
import java.awt.Component
import java.net.URI
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

                val menu = JMenu(label)
                menu.icon = icon
                menu.toolTipText = description
                menu.add(JiraWorkLogUI(tasks, JiraCommitTimeService(username, password, url)))

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
        )
    }

    override fun name(): String = "Внесение отработанного времени в Jira"

    override fun icon(): Icon? = icon

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val USERNAME = RocketActionConfigurationPropertyKey("username")
        private val PASSWORD = RocketActionConfigurationPropertyKey("password")
        private val URL = RocketActionConfigurationPropertyKey("url")

        private val PREDEFINED_TASKS = RocketActionConfigurationPropertyKey("predefinedTasks")
    }
}
