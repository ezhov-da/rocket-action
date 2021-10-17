package ru.ezhov.rocket.action.types.urlparser

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import ru.ezhov.rocket.action.url.parser.UrlParser
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger {}

class UrlParserRocketAction : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): RocketAction? =
            settings.settings()[LABEL]
                    ?.takeIf { it.isNotEmpty() && settings.type().value() == this.type().value() }
                    ?.let { label ->
                        val headers = settings.settings()[HEADERS]
                                ?.split("\n")
                                ?.mapNotNull { v ->
                                    v.split(DELIMITER)
                                            .takeIf { it.size == 2 }
                                            ?.let { Pair(first = it[0], second = it[1]) } ?: run {
                                        logger.warn { "Header $v is not standard" }
                                        null
                                    }
                                }
                                ?.toMap()
                                .orEmpty()
                        object : RocketAction {
                            override fun contains(search: String): Boolean = label.contains(search)

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                    !(settings.id() == actionSettings.id() &&
                                            settings.settings() == actionSettings.settings())

                            override fun component(): Component = JMenu(label).apply {
                                val panelTop = JPanel(BorderLayout())
                                icon = IconRepositoryFactory.repository.by(AppIcon.FLASH)
                                val textField = TextFieldWithText("URL")
                                textField.columns = 15
                                val txt = JTextPane()
                                val button = JButton("Получить описание").apply {
                                    addActionListener(
                                            ButtonListener(
                                                    callbackUrl = { textField.text },
                                                    callbackSetText = { text ->
                                                        SwingUtilities.invokeLater { txt.text = text }
                                                    },
                                                    headers = headers
                                            )
                                    )
                                }
                                panelTop.add(textField, BorderLayout.NORTH)
                                panelTop.add(button, BorderLayout.SOUTH)
                                val panel = JPanel(BorderLayout()).apply {
                                    val dimension = Dimension(300, 150)
                                    minimumSize = dimension
                                    maximumSize = dimension
                                    preferredSize = dimension
                                }
                                panel.add(panelTop, BorderLayout.NORTH)
                                panel.add(JScrollPane(txt), BorderLayout.CENTER)

                                add(panel)
                            }
                        }
                    }

    override fun type(): RocketActionType = RocketActionType { "URL_PARSER" }

    override fun name(): String = "Получение описания URL"

    override fun description(): String = "Получение заголовка и описания URL"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> = listOf(
            createRocketActionProperty(
                    key = LABEL,
                    name = "Название",
                    description = "название для отображения",
                    required = true,
                    default = "Получить заголовок URL"
            ),
            createRocketActionProperty(
                    key = HEADERS,
                    name = "Заголовки",
                    description =
                    """Заголовки для запроса в формате - каждый заголовок с новой строки: 
                        |Имя${DELIMITER}Значение
                    """.trimMargin(),
                    required = false,
            )
    )

    companion object {
        private const val DELIMITER = "_____"
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val HEADERS = RocketActionConfigurationPropertyKey("headers")
    }

    private class ButtonListener(
            private val callbackUrl: () -> String,
            private val callbackSetText: (value: String) -> Unit,
            private val headers: Map<String, String>
    ) : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            UrlWorker(
                    button = e!!.source as JButton,
                    url = callbackUrl(),
                    callbackSetText = callbackSetText,
                    headers = headers
            ).execute()
        }

        private class UrlWorker(
                private val button: JButton,
                private val url: String,
                private val callbackSetText: (value: String) -> Unit,
                private val headers: Map<String, String>,
        ) : SwingWorker<String, String>() {
            init {
                button.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
            }

            override fun doInBackground(): String = UrlParser(url, headers).parse()

            override fun done() {
                try {
                    callbackSetText(get())
                } catch (ex: Exception) {
                    logger.warn(ex) {}

                    NotificationFactory.notification.show(
                            type = NotificationType.ERROR,
                            text = "Ошибка получения описания для URL '$url'"
                    )
                }
                button.icon = null
            }
        }
    }
}