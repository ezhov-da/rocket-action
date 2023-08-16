package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import arrow.core.getOrHandle
import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import org.jdesktop.swingx.JXCollapsiblePane
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineFactory
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import ru.ezhov.rocket.action.plugin.script.dynamic.FieldNamesService
import ru.ezhov.rocket.action.ui.utils.swing.common.TextPaneWithText
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextArea
import javax.swing.JTextPane
import javax.swing.JToolBar
import javax.swing.SwingWorker

private val logger = KotlinLogging.logger { }

class UiService {
    companion object {
        fun build(settings: RocketActionSettings, context: RocketActionContext): RocketAction? {
            val label = settings.settings()[DynamicScriptRocketActionUi.LABEL].orEmpty()
            val script = settings.settings()[DynamicScriptRocketActionUi.SCRIPT].orEmpty()
            val description = settings.settings()[DynamicScriptRocketActionUi.DESCRIPTION].orEmpty()
            val countVariables = settings.settings()[DynamicScriptRocketActionUi.COUNT_VARIABLES]?.toIntOrNull() ?: 0
            val selectedScriptLang =
                settings.settings()[DynamicScriptRocketActionUi.SELECTED_SCRIPT_LANG] ?: ScriptEngineType.GROOVY.name
            val fieldNames = settings.settings()[DynamicScriptRocketActionUi.FIELD_NAMES].orEmpty()
            val instruction = settings.settings()[DynamicScriptRocketActionUi.INSTRUCTION].orEmpty()

            val menu = buildMenu(
                label = label,
                script = script,
                countVariables = countVariables,
                selectedScriptLang = selectedScriptLang,
                fieldNames = fieldNames,
                instruction = instruction,
                context = context,
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

                override fun handler(): RocketActionHandler? = null // TODO ezhov is not needed yet
            }
        }

        private fun buildMenu(
            label: String,
            script: String,
            countVariables: Int,
            selectedScriptLang: String,
            fieldNames: String,
            instruction: String,
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
                        instruction = instruction,
                        context = context,
                    )
                )
            }

            return menu
        }
    }

    private class ScriptPanel(
        private val script: String,
        countVariables: Int,
        selectedScriptLang: String,
        fieldNames: String,
        instruction: String,
        context: RocketActionContext,
    ) : JPanel(BorderLayout()) {
        private val infoLabel = JLabel()
        private val resultText = RSyntaxTextArea()
        private val engineType = ScriptEngineType.valueOf(selectedScriptLang)
        private val engine = ScriptEngineFactory.engine(engineType)

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
            val iconLoader = ImageIcon(UiService::class.java.getResource("/icon/loader.gif"))

            val executeFunc = {
                val variables = variablesField
                    .mapIndexed { index, variable ->
                        "_v${index + 1}" to variable.value()
                    }.toMap()

                infoLabel.text = "Execution started"
                infoLabel.icon = iconLoader
                executeScript(
                    variables = variables,
                    context = context,
                    callback = { result ->
                        resultText.text = result
                        infoLabel.text = "Done"
                        infoLabel.icon = null
                    }
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
                                textPane = TextPaneWithText("Name not set"),
                                defaultValue = "",
                            )
                    )
                }
                variablesField.forEach { add(JScrollPane(it.textPane)) }
            }

            val instructionCollapsiblePane = JXCollapsiblePane()
            val topPanel = JPanel(BorderLayout()).apply {
                if (instruction.isNotEmpty()) {
                    val instructionPanel = JPanel(BorderLayout()).apply {
                        add(
                            JTextArea().apply {
                                isEditable = false
                                text = instruction
                                foreground = JLabel().foreground
                            },
                            BorderLayout.CENTER
                        )
                    }
                    instructionCollapsiblePane.isAnimated = true
                    instructionCollapsiblePane.isCollapsed = true
                    instructionCollapsiblePane.add(instructionPanel)
                    add(instructionCollapsiblePane, BorderLayout.NORTH)
                }

                add(variablesPanel, BorderLayout.CENTER)
            }

            add(
                JToolBar().apply {
                    isFloatable = false
                    add(
                        JButton("Run")
                            .apply {
                                addActionListener {
                                    executeFunc.invoke()
                                }
                            }
                    )

                    add(
                        JButton("Restore defaults")
                            .apply {
                                addActionListener {
                                    variablesField.forEach { it.restore() }
                                }
                            }
                    )

                    add(
                        JButton("Copy result to clipboard")
                            .apply {
                                addActionListener {
                                    if (resultText.text.isNotBlank()) {
                                        val defaultToolkit = Toolkit.getDefaultToolkit()
                                        val clipboard = defaultToolkit.systemClipboard
                                        clipboard.setContents(StringSelection(resultText.text), null)
                                        context.notification().show(
                                            type = NotificationType.INFO,
                                            text = "Text copied to clipboard"
                                        )
                                    } else {
                                        context.notification().show(
                                            type = NotificationType.WARN,
                                            text = "Missing text to copy"
                                        )
                                    }
                                }
                            }
                    )

                    if (instruction.isNotEmpty()) {
                        add(
                            JButton("Instruction")
                                .apply {
                                    addActionListener {
                                        instructionCollapsiblePane.isCollapsed = !instructionCollapsiblePane.isCollapsed
                                    }
                                }
                        )
                    }
                },
                BorderLayout.NORTH
            )

            val bottomPanel = JPanel(BorderLayout()).apply {
                add(RTextScrollPane(resultText), BorderLayout.CENTER)
                add(infoLabel, BorderLayout.SOUTH)
            }

            add(JPanel(BorderLayout()).apply {
                add(
                    JSplitPane(JSplitPane.VERTICAL_SPLIT).apply {
                        topComponent = topPanel
                        bottomComponent = bottomPanel
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
                        "Runtime error"
                    }

                override fun done() {
                    callback(get())
                }
            }.execute()
        }
    }
}
