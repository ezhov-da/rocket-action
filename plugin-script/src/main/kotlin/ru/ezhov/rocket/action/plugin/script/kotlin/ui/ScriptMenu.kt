package ru.ezhov.rocket.action.plugin.script.kotlin.ui

import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import javax.swing.JButton
import javax.swing.JMenu
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSplitPane
import javax.swing.JTextPane

class ScriptMenu(
    label: String,
    script: String,
    description: String? = null,
    executeOnLoad: Boolean = false,
) : JMenu(label) {
    companion object {
        val ICON_DEFAULT = IconRepositoryFactory.repository.by(AppIcon.BOLT)
    }

    private val panelExecute: PanelExecute?

    init {
        description?.let { toolTipText = it }
        panelExecute = PanelExecute(
            beforeExecuteCallback = beforeExecuteCallBack(),
            afterExecuteCallback = afterExecuteCallBack(),
            script = script,
        )
        add(panelExecute)
        icon = ICON_DEFAULT

        if (executeOnLoad) {
            ScriptSwingWorker(
                beforeExecuteCallback = beforeExecuteCallBack(),
                afterExecuteCallback = afterExecuteCallBack(),
                script = script,
            )
                .execute()
        }
    }

    private fun beforeExecuteCallBack(): () -> Unit = {
        icon = IconRepositoryFactory.repository.by(AppIcon.LOADER)
    }

    private fun afterExecuteCallBack(): (String) -> Unit = {
        icon = ICON_DEFAULT
        panelExecute?.setText(it)
    }

    fun executeScript() {
        panelExecute?.executeScript()
    }

    private class PanelExecute(
        private val beforeExecuteCallback: () -> Unit,
        private val afterExecuteCallback: (String) -> Unit,
        script: String,
    ) : JPanel() {
        val textPaneScript = JTextPane()
        val textPaneResult = JTextPane()
        val buttonExecute = JButton("Выполнить")

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
            splitPanel.topComponent = JScrollPane(textPaneScript)
            val panelBottom = JPanel(BorderLayout()).apply {
                add(buttonExecute, BorderLayout.NORTH)
                add(JScrollPane(textPaneResult), BorderLayout.CENTER)
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
            )
                .execute()
        }
    }

}
