package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
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

    init {
        add(idTextField, "grow, width 100%")
        add(copyIdButton, "wrap");
        add(RTextScrollPane(descriptionTextPane, false), "wrap, span, width 100%, height 100%")

        val dimension = SizeUtil.dimension(0.2, 0.2)
        size = dimension
        preferredSize = dimension
        minimumSize = dimension
        maximumSize = dimension
    }
}
