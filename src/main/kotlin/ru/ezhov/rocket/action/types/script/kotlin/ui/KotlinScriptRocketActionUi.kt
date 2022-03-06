package ru.ezhov.rocket.action.types.script.kotlin.ui

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
import java.awt.BorderLayout
import java.awt.Component
import javax.script.ScriptEngineManager
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JTextPane
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger {}

class KotlinScriptRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): RocketAction? = run {
        settings.settings()[SCRIPT]
            ?.takeIf { it.isNotEmpty() }
            ?.let { script ->
                settings.settings()[LABEL]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { label ->
                        val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: script
                        val menu = JMenu(label).apply {
                            toolTipText = description
                        }
                        val panelExecute = PanelExecute(menu, script)
                        menu.add(panelExecute)

                        ScriptLoader(menu = menu, script = script, panelExecute = panelExecute).execute()

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
            }
    }

    private class ScriptLoader(
        private val menu: JMenu,
        private val panelExecute: PanelExecute,
        private val script: String,
    ) : SwingWorker<Any, Any>() {
        init {
            menu.icon = IconRepositoryFactory.repository.by(AppIcon.LOADER)
        }

        override fun doInBackground(): Any {
            val scriptEngine = ScriptEngineManager().getEngineByExtension("kts")
            return scriptEngine.eval(script)
        }

        override fun done() {
            try {
                val textAsObject = this.get()
                val text = textAsObject?.toString() ?: "null result"
                menu.icon = IconRepositoryFactory.repository.by(AppIcon.BOLT)
                panelExecute.setText(text)
            } catch (ex: Exception) {
                logger.warn(ex) { "Error script executed. Script $script" }
                NotificationFactory.notification.show(NotificationType.WARN, "Ошибка выполнения скрипта")
            }
        }
    }

    private class PanelExecute(parentMenu: JMenu, script: String) : JPanel() {
        val textPane = JTextPane()
        val buttonExecute = JButton("Выполнить")

        init {
            layout = BorderLayout()
            buttonExecute.addActionListener {
                ScriptLoader(menu = parentMenu, script = script, panelExecute = this).execute()
            }

            add(textPane, BorderLayout.NORTH)
            add(buttonExecute, BorderLayout.SOUTH)
        }

        fun setText(text: String) {
            textPane.text = text
        }
    }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Выполнение скрипта Kotlin"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, SCRIPT)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = SCRIPT, name = SCRIPT.value, description = "Скрипт для выполнения", required = true),
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Заголовок", required = true),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION.value, description = "Описание", required = false),
        )
    }

    override fun name(): String = "Kotlin script"

    companion object {
        const val TYPE = "KOTLIN_SCRIPT"
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val SCRIPT = RocketActionConfigurationPropertyKey("script")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
    }
}