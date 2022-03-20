package ru.ezhov.rocket.action.plugin.text

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
import ru.ezhov.rocket.action.icon.toImage
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
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
    private val iconDef = IconRepositoryFactory.repository.by(AppIcon.TEXT)
    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun description(): String = "Show text"

    override fun properties(): List<RocketActionConfigurationProperty> =
        listOf(
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Заголовок", required = true),
            createRocketActionProperty(key = TEXT, name = TEXT.value, description = "Текст", required = true),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION.value, description = "Описание", required = false),
        )

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
                val description = settings.settings()[DESCRIPTION]
                object : RocketAction {
                    override fun contains(search: String): Boolean =
                        label.contains(search, ignoreCase = true)
                            .or(text.contains(search, ignoreCase = true))
                            .or(description?.contains(search, ignoreCase = true) ?: false)

                    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                        !(settings.id() == actionSettings.id() &&
                            settings.settings() == actionSettings.settings())

                    override fun component(): Component = JMenu(label).apply {
                        this.icon = iconDef
                        val panel = JPanel(BorderLayout())
                        panel.add(
                            JToolBar().apply {
                                add(JButton(
                                    object : AbstractAction() {
                                        init {
                                            putValue(SHORT_DESCRIPTION, "Скопировать текст в буфер")
                                            putValue(SMALL_ICON, IconRepositoryFactory.repository.by(AppIcon.COPY_WRITING))
                                        }

                                        override fun actionPerformed(e: ActionEvent?) {
                                            val defaultToolkit = Toolkit.getDefaultToolkit()
                                            val clipboard = defaultToolkit.systemClipboard
                                            clipboard.setContents(StringSelection(text), null)
                                            NotificationFactory.notification.show(
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
                                            putValue(SMALL_ICON, IconRepositoryFactory.repository.by(AppIcon.ARROW_TOP))
                                        }

                                        override fun actionPerformed(e: ActionEvent?) {
                                            SwingUtilities.invokeLater {
                                                val dimension = Toolkit.getDefaultToolkit().screenSize
                                                val frame = JFrame(label)
                                                frame.iconImage = IconRepositoryFactory
                                                    .repository.by(AppIcon.ROCKET_APP)
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
                }
            }
        }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun type(): RocketActionType = RocketActionType { "SHOW_TEXT_AS_MENU" }

    override fun icon(): Icon? = iconDef

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val TEXT = RocketActionConfigurationPropertyKey("text")
    }

    override fun name(): String = "Отобразить текст как подпункт меню"
}