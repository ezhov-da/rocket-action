package ru.ezhov.rocket.action.plugin.urlparser

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
import ru.ezhov.rocket.action.plugin.url.parser.UrlParser
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserFilter
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserResult
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger {}

class UrlParserRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val iconDef = IconRepositoryFactory.repository.by(AppIcon.FLASH)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[LABEL]
            ?.takeIf { it.isNotEmpty() && settings.type().value() == this.type().value() }
            ?.let { label ->
                val headers = settings
                    .settings()[HEADERS]
                    ?.split("\n")
                    ?.mapNotNull { header ->
                        if (header.isNotBlank()) {
                            header.split(DELIMITER)
                                .takeIf { it.size == 2 }
                                ?.let { Pair(first = it[0], second = it[1]) }
                                ?: run {
                                    logger.warn { "Header $header is not standard" }
                                    null
                                }
                        } else null
                    }
                    ?.toMap()
                    .orEmpty()

                val component = JMenu(label).apply {
                    val panelTop = JPanel(BorderLayout())
                    icon = iconDef
                    val textField = TextFieldWithText("URL")
                    textField.columns = 15
                    val txt = JTextPane()
                    val button = JButton("???????????????? ????????????????").apply {
                        addActionListener(
                            ButtonListener(
                                callbackUrl = { textField.text },
                                callbackSetText = { result ->
                                    SwingUtilities.invokeLater {
                                        txt.text = """
                                        ${result.title}
                                        ${result.description}
                                    """.trimIndent()
                                    }
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
                object : RocketAction {
                    override fun contains(search: String): Boolean = label.contains(search)

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = component
                }
            }

    override fun type(): RocketActionType = RocketActionType { "URL_PARSER" }

    override fun name(): String = "?????????????????? ???????????????? URL"

    override fun description(): String = "?????????????????? ?????????????????? ?? ???????????????? URL"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun icon(): Icon? = iconDef

    override fun properties(): List<RocketActionConfigurationProperty> = listOf(
        createRocketActionProperty(
            key = LABEL,
            name = "????????????????",
            description = "???????????????? ?????? ??????????????????????",
            required = true,
            property = RocketActionPropertySpec.StringPropertySpec(defaultValue = "???????????????? ?????????????????? URL"),
        ),
        createRocketActionProperty(
            key = HEADERS,
            name = "??????????????????",
            description =
            """?????????????????? ?????? ?????????????? ?? ?????????????? - ???????????? ?????????????????? ?? ?????????? ????????????:
                        |??????${DELIMITER}????????????????
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
        private val callbackSetText: (value: UrlParserResult) -> Unit,
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
            private val callbackSetText: (value: UrlParserResult) -> Unit,
            private val headers: Map<String, String>,
        ) : SwingWorker<UrlParserResult, UrlParserResult>() {
            init {
                button.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
            }

            override fun doInBackground(): UrlParserResult =
                UrlParser(url, headers).parse(UrlParserFilter(readTitle = true, readDescription = true))

            override fun done() {
                try {
                    callbackSetText(get())
                } catch (ex: Exception) {
                    logger.warn(ex) {}

                    NotificationFactory.notification.show(
                        type = NotificationType.ERROR,
                        text = "???????????? ?????????????????? ???????????????? ?????? URL '$url'"
                    )
                }
                button.icon = null
            }
        }
    }
}
