package ru.ezhov.rocket.action.plugin.script.ui

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.plugin.script.ScriptEngine
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JSplitPane

class ScriptMenu(
    label: String,
    script: String,
    description: String? = null,
    executeOnLoad: Boolean = false,
    private val context: RocketActionContext,
    scriptEngine: ScriptEngine,
) : JMenu(label) {
    private val iconDefault = context.icon().by(AppIcon.BOLT)

    private val panelExecute: PanelExecute?

    init {
        description?.let { toolTipText = it }
        panelExecute = PanelExecute(
            beforeExecuteCallback = beforeExecuteCallBack(),
            afterExecuteCallback = afterExecuteCallBack(),
            script = script,
            context = context,
            scriptEngine = scriptEngine,
            variables = context.variables().variables(),
        )
        add(panelExecute)
        icon = iconDefault

        if (executeOnLoad) {
            ScriptSwingWorker(
                beforeExecuteCallback = beforeExecuteCallBack(),
                afterExecuteCallback = afterExecuteCallBack(),
                script = script,
                context = context,
                scriptEngine = scriptEngine,
                variables = context.variables().variables(),
            )
                .execute()
        }
    }

    private fun beforeExecuteCallBack(): () -> Unit = {
        icon = context.icon().by(AppIcon.LOADER)
    }

    private fun afterExecuteCallBack(): (String) -> Unit = {
        icon = iconDefault
        panelExecute?.setText(it)
    }

    fun executeScript() {
        panelExecute?.executeScript()
    }

    private class PanelExecute(
        private val beforeExecuteCallback: () -> Unit,
        private val afterExecuteCallback: (String) -> Unit,
        script: String,
        private val context: RocketActionContext,
        private val scriptEngine: ScriptEngine,
        private val variables: Map<String, String>,
    ) : JPanel() {
        val textPaneScript = RSyntaxTextArea().apply {
            syntaxEditingStyle = when (scriptEngine.type()) {
                ScriptEngineType.KOTLIN -> SyntaxConstants.SYNTAX_STYLE_KOTLIN
                ScriptEngineType.GROOVY -> SyntaxConstants.SYNTAX_STYLE_KOTLIN
            }
            isCodeFoldingEnabled = true
        }
        val textPaneResult = RSyntaxTextArea().apply {
            isCodeFoldingEnabled = true
        }
        val buttonExecute = JButton("Run")

        init {
            layout = BorderLayout()
            val screenSize = Toolkit.getDefaultToolkit().screenSize
            val panelDimension = Dimension((screenSize.width * 0.3).toInt(), (screenSize.height * 0.5).toInt())
            size = panelDimension
            minimumSize = panelDimension
            preferredSize = panelDimension
            maximumSize = panelDimension
            textPaneScript.text = script
            buttonExecute
                .addActionListener { executeScript() }

            val splitPanel = JSplitPane(JSplitPane.VERTICAL_SPLIT)
            splitPanel.topComponent = RTextScrollPane(textPaneScript)
            val panelBottom = JPanel(BorderLayout()).apply {
                add(buttonExecute, BorderLayout.NORTH)
                add(RTextScrollPane(textPaneResult), BorderLayout.CENTER)
            }
            splitPanel.bottomComponent = panelBottom
            splitPanel.setDividerLocation(0.3)
            splitPanel.resizeWeight = 0.3

            add(splitPanel, BorderLayout.CENTER)
        }

        fun setText(text: String) {
            textPaneResult.text = text
        }

        fun executeScript() {
            ScriptSwingWorker(
                beforeExecuteCallback = beforeExecuteCallback,
                afterExecuteCallback = afterExecuteCallback,
                script = textPaneScript.text,
                context = context,
                scriptEngine = scriptEngine,
                variables = variables,
            )
                .execute()
        }
    }

}
