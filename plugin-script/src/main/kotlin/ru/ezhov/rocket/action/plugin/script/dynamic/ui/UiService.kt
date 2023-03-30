package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import arrow.core.getOrHandle
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import ru.ezhov.rocket.action.plugin.script.dynamic.FieldNamesService
import ru.ezhov.rocket.action.ui.utils.swing.common.TextPaneWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger { }

class UiService {
    fun build(settings: RocketActionSettings, context: RocketActionContext): RocketAction? {
        val label = settings.settings()[DynamicScriptRocketActionUi.LABEL].orEmpty()
        val script = settings.settings()[DynamicScriptRocketActionUi.SCRIPT].orEmpty()
        val description = settings.settings()[DynamicScriptRocketActionUi.DESCRIPTION].orEmpty()
        val countVariables = settings.settings()[DynamicScriptRocketActionUi.COUNT_VARIABLES]?.toIntOrNull() ?: 0
        val selectedScriptLang =
            settings.settings()[DynamicScriptRocketActionUi.SELECTED_SCRIPT_LANG] ?: ScriptEngineType.GROOVY.name
        val fieldNames = settings.settings()[DynamicScriptRocketActionUi.FIELD_NAMES].orEmpty()

        val menu = buildMenu(
            label = label,
            script = script,
            description = description,
            countVariables = countVariables,
            selectedScriptLang = selectedScriptLang,
            fieldNames = fieldNames,
            context = context
        )

        context.search().register(settings.id(), label)
        context.search().register(settings.id(), description)

        return object : RocketAction, RocketActionHandlerFactory {
            override fun contains(search: String): Boolean =
                label.contains(search, ignoreCase = true)
                    .or(description.contains(search, ignoreCase = true))

            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                !(settings.id() == actionSettings.id() &&
                    settings.settings() == actionSettings.settings())

            override fun component(): Component = menu

            override fun handler(): RocketActionHandler? = null // TODO ezhov пока не нужен
        }
    }

    private fun buildMenu(
        label: String,
        script: String,
        description: String,
        countVariables: Int,
        selectedScriptLang: String,
        fieldNames: String,
        context: RocketActionContext
    ): JMenu {
        val menu = JMenu(label).apply {
            icon = context.icon().by(AppIcon.BOLT)
            add(
                ScriptPanel(
                    script = script,
                    countVariables = countVariables,
                    selectedScriptLang = selectedScriptLang,
                    fieldNames = fieldNames,
                    context = context,
                )
            )
        }

        return menu
    }

    private class ScriptPanel(
        private val script: String,
        countVariables: Int,
        selectedScriptLang: String,
        fieldNames: String,
        context: RocketActionContext,
    ) : JPanel(BorderLayout()) {
        private val resultText = RSyntaxTextArea()
        val engineType = ScriptEngineType.valueOf(selectedScriptLang)
        val engine = ScriptEngineFactory.engine(engineType)

        init {
            data class TextPaneAndDefaultValue(
                val textPane: JTextPane,
                val defaultValue: String,
            ) {
                fun restore() {
                    textPane.text = defaultValue
                }

                fun value(): String = textPane.text
            }

            val variablesField: MutableList<TextPaneAndDefaultValue> = mutableListOf()

            val executeFunc = {
                val variables = variablesField
                    .mapIndexed { index, variable ->
                        "_v${index + 1}" to variable.value()
                    }.toMap()
                executeScript(
                    variables = variables,
                    context = context,
                    callback = { result -> resultText.text = result }
                )
            }


            val variablesPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)

                val names = FieldNamesService().get(fieldNames, ":")
                (0 until countVariables).forEach { index ->
                    variablesField.add(
                        names.getOrNull(index)?.let {
                            TextPaneAndDefaultValue(
                                textPane = TextPaneWithText(it.name).apply { text = it.value },
                                defaultValue = it.value,
                            )
                        }
                            ?: TextPaneAndDefaultValue(
                                textPane = TextPaneWithText("Не задано название"),
                                defaultValue = "",
                            )
                    )
                }
                variablesField.forEach { add(JScrollPane(it.textPane)) }
            }

            add(
                JToolBar().apply {
                    isFloatable = false
                    add(
                        JButton("Выполнить")
                            .apply {
                                addActionListener {
                                    executeFunc.invoke()
                                }
                            }
                    )

                    add(
                        JButton("Восстановить значения по умолчанию")
                            .apply {
                                addActionListener {
                                    variablesField.forEach { it.restore() }
                                }
                            }
                    )
                },
                BorderLayout.NORTH
            )

            add(JPanel(BorderLayout()).apply {
                add(
                    JSplitPane(JSplitPane.VERTICAL_SPLIT).apply {
                        topComponent = variablesPanel
                        bottomComponent = RTextScrollPane(resultText)
                    }, BorderLayout.CENTER
                )
            }, BorderLayout.CENTER)

            val dimension = Dimension(700, 400)
            size = dimension
            minimumSize = dimension
            maximumSize = dimension
            preferredSize = dimension
        }

        private fun executeScript(
            variables: Map<String, String>,
            context: RocketActionContext,
            callback: (text: String) -> Unit
        ) {
            context.variables().variables().toMutableMap() + variables

            object : SwingWorker<String, String>() {
                override fun doInBackground(): String =
                    try {
                        engine.execute(
                            script = script,
                            variables = context.variables().variables().toMutableMap() + variables
                        ).getOrHandle { throw it }?.toString().orEmpty()
                    } catch (ex: Exception) {
                        logger.error(ex) { "Error when execute script" }
                        "Ошибка выполнения"
                    }

                override fun done() {
                    callback(get())
                }
            }.execute()
        }
    }
}
