package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import arrow.core.getOrHandle
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import ru.ezhov.rocket.action.ui.utils.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JTextPane
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
                    description = description,
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
        description: String,
        countVariables: Int,
        selectedScriptLang: String,
        fieldNames: String,
        context: RocketActionContext,
    ) : JPanel(BorderLayout()) {
        private val resultText = JTextPane()
        val engineType = ScriptEngineType.valueOf(selectedScriptLang)
        val engine = ScriptEngineFactory.engine(engineType)

        init {
            val variablesField: MutableList<JTextField> = mutableListOf()

            val executeFunc = {
                val variables = variablesField
                    .mapIndexed { index, jTextField ->
                        "_v${index + 1}" to jTextField.text
                    }.toMap()
                executeScript(
                    variables = variables,
                    context = context,
                    callback = { result -> resultText.text = result }
                )
            }


            val variablesPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                val names = fieldNames.split("\n")
                (0 until countVariables).forEach { index ->
                    variablesField.add(
                        TextFieldWithText(names.getOrNull(index) ?: "Не задано название")
                            .apply {
                                addKeyListener(object : KeyAdapter() {
                                    override fun keyPressed(e: KeyEvent) {
                                        if (e.keyCode == KeyEvent.VK_ENTER) {
                                            executeFunc.invoke()
                                        }
                                    }
                                })
                            }
                    )
                }
                variablesField.forEach { add(it) }
                add(
                    JButton("Execute")
                        .apply {
                            addActionListener {
                                executeFunc.invoke()
                            }
                        }
                )
            }

            add(variablesPanel, BorderLayout.NORTH)
            add(JScrollPane(resultText), BorderLayout.CENTER)

            val dimension = Dimension(400, 600)
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
