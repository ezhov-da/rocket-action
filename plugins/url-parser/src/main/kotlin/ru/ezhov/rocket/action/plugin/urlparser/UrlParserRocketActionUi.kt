package ru.ezhov.rocket.action.plugin.urlparser

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.url.parser.UrlParser
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserFilter
import ru.ezhov.rocket.action.plugin.url.parser.UrlParserResult
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
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
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-url-parser.properties"))
        object : RocketActionPluginInfo {
            override fun version(): String = properties.getProperty("version")

            override fun author(): String = properties.getProperty("author")

            override fun link(): String? = properties.getProperty("link")
        }
    }

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
                    icon = actionContext!!.icon().by(AppIcon.FLASH)
                    val textField = TextFieldWithText("URL")
                    textField.columns = 15
                    val txt = JTextPane()
                    val button = JButton("Get description").apply {
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
                                headers = headers,
                                context = context,
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

                context.search().register(settings.id(), label)

                object : RocketAction {
                    override fun contains(search: String): Boolean = label.contains(search)

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = component
                }
            }

    override fun type(): RocketActionType = RocketActionType { "URL_PARSER" }

    override fun name(): String = "Getting the description of a URL"

    override fun description(): String = "Getting the title and description of a URL"

    override fun asString(): List<String> = listOf(LABEL)

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.FLASH)

    override fun properties(): List<RocketActionConfigurationProperty> = listOf(
        createRocketActionProperty(
            key = LABEL,
            name = "Title",
            description = "Title to display",
            required = true,
            property = RocketActionPropertySpec.StringPropertySpec(defaultValue = "Get URL title"),
        ),
        createRocketActionProperty(
            key = HEADERS,
            name = "Titles",
            description = "Headers for the request in the format - each header on a new line: Name${DELIMITER}Value",
            required = false,
        )
    )

    companion object {
        private const val DELIMITER = "_____"
        private val LABEL = "label"
        private val HEADERS = "headers"
    }

    private class ButtonListener(
        private val callbackUrl: () -> String,
        private val callbackSetText: (value: UrlParserResult) -> Unit,
        private val headers: Map<String, String>,
        private val context: RocketActionContext,
    ) : ActionListener {
        override fun actionPerformed(e: ActionEvent?) {
            UrlWorker(
                button = e!!.source as JButton,
                url = callbackUrl(),
                callbackSetText = callbackSetText,
                headers = headers,
                context = context,
            ).execute()
        }

        private class UrlWorker(
            private val button: JButton,
            private val url: String,
            private val callbackSetText: (value: UrlParserResult) -> Unit,
            private val headers: Map<String, String>,
            private val context: RocketActionContext,
        ) : SwingWorker<UrlParserResult, UrlParserResult>() {
            init {
                button.icon = ImageIcon(this.javaClass.getResource("/icons/load_16x16.gif"))
            }

            override fun doInBackground(): UrlParserResult =
                UrlParser(url, headers).parse(UrlParserFilter(readTitle = true, readDescription = true))

            override fun done() {
                try {
                    callbackSetText(get())
                } catch (ex: Exception) {
                    logger.warn(ex) {}

                    context.notification().show(
                        type = NotificationType.ERROR,
                        text = "Error getting description for URL '$url'"
                    )
                }
                button.icon = null
            }
        }
    }
}
