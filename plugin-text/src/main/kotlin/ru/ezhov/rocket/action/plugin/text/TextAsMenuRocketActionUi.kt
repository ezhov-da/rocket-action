package ru.ezhov.rocket.action.plugin.text

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class TextAsMenuRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
        listOf(
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Заголовок", required = true),
            createRocketActionProperty(key = TEXT, name = TEXT, description = "Текст", required = true),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION, description = "Описание", required = false),
        )

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
                val description = settings.settings()[DESCRIPTION]
                val menu = JMenu(label).apply {
                    this.icon = actionContext!!.icon().by(AppIcon.TEXT)
                    val panel = JPanel(BorderLayout())
                    panel.add(
                        JToolBar().apply {
                            add(JButton(
                                object : AbstractAction() {
                                    init {
                                        putValue(SHORT_DESCRIPTION, "Скопировать текст в буфер")
                                        putValue(SMALL_ICON, actionContext!!.icon().by(AppIcon.COPY_WRITING))
                                    }

                                    override fun actionPerformed(e: ActionEvent?) {
                                        val defaultToolkit = Toolkit.getDefaultToolkit()
                                        val clipboard = defaultToolkit.systemClipboard
                                        clipboard.setContents(StringSelection(text), null)
                                        actionContext!!.notification().show(
                                            type = NotificationType.INFO,
                                            text = "Текст скопирован в буфер"
                                        )
                                    }
                                }
                            ))
                            add(JButton(
                                object : AbstractAction() {
                                    init {
                                        putValue(SHORT_DESCRIPTION, "Открыть в отдельном окне")
                                        putValue(SMALL_ICON, actionContext!!.icon().by(AppIcon.ARROW_TOP))
                                    }

                                    override fun actionPerformed(e: ActionEvent?) {
                                        SwingUtilities.invokeLater {
                                            val dimension = Toolkit.getDefaultToolkit().screenSize
                                            val frame = JFrame(label)
                                            frame.iconImage = actionContext!!
                                                .icon()
                                                .by(AppIcon.ROCKET_APP)
                                                .toImage()
                                            frame.add(JScrollPane(JTextPane().apply {
                                                this.text = text
                                                isEditable = false
                                            }))
                                            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
                                            frame.setSize(
                                                (dimension.width * 0.8).toInt(),
                                                (dimension.height * 0.8).toInt()
                                            )
                                            frame.setLocationRelativeTo(null)
                                            frame.isVisible = true
                                        }
                                    }
                                }
                            ))
                        },
                        BorderLayout.NORTH
                    )
                    panel.add(
                        JTextPane().apply {
                            val textPane = this
                            textPane.text = text
                            isEditable = false
                            background = JLabel().background
                        },
                        BorderLayout.CENTER
                    )

                    this.add(panel)
                }

                context.search().register(settings.id(), label)
                context.search().register(settings.id(), text)
                description?.let { context.search().register(settings.id(), it) }

                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(text.contains(search, ignoreCase = true))
                            .or(description?.contains(search, ignoreCase = true) ?: false)

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = menu
                }
            }
        }

    override fun asString(): List<String> = listOf(LABEL)

    override fun type(): RocketActionType = RocketActionType { "SHOW_TEXT_AS_MENU" }

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.TEXT)

    companion object {
        private val LABEL = "label"
        private val DESCRIPTION = "description"
        private val TEXT = "text"
    }

    override fun name(): String = "Отобразить текст как подпункт меню"
}
