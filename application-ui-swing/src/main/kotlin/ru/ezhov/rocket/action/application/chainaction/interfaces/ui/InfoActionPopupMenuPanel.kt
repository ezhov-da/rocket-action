package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.plugin.clipboard.ClipboardUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

class InfoActionPopupMenuPanel(
    private val action: Action,
) : JPanel(MigLayout(/*"debug"*/)) {
    private val idTextField = JTextField().also {
        it.isEditable = false
        it.text = action.id()
    }

    private val copyIdButton: JButton = JButton(Icons.Advanced.COPY_16x16).apply {
        toolTipText = "Copy ID to clipboard"
        addActionListener {
            ClipboardUtil.copyToClipboard(idTextField.text)
        }
    }

    private val descriptionTextPane = RSyntaxTextArea().also {
        it.isEditable = false
        it.text = action.description()
    }

    private val dataTextPane = RSyntaxTextArea().also {
        it.isEditable = false
    }

    init {
        add(idTextField, "grow, width 100%")
        add(copyIdButton, "wrap")

        when (action) {
            is AtomicAction -> {
                dataTextPane.syntaxEditingStyle = when (action.engine) {
                    AtomicActionEngine.KOTLIN -> SyntaxConstants.SYNTAX_STYLE_KOTLIN
                    AtomicActionEngine.GROOVY -> SyntaxConstants.SYNTAX_STYLE_GROOVY
                }
                dataTextPane.text = action.data
                add(RTextScrollPane(descriptionTextPane, false), "wrap, span, width 100%, height 40%")
                add(RTextScrollPane(dataTextPane), "wrap, span, width 100%, height 100%")
            }

            else -> {
                add(RTextScrollPane(descriptionTextPane, false), "wrap, span, width 100%, height 100%")
            }
        }

        val dimension = SizeUtil.dimension(0.3, 0.4)
        size = dimension
        preferredSize = dimension
        minimumSize = dimension
        maximumSize = dimension
    }
}
