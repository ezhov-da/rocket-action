package ru.ezhov.rocket.action.ui.utils.swing.common

import java.awt.Color
import java.awt.Graphics
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.JTextField

open class TextFieldWithText(var placeholder: String = "") : JTextField() {
    init {
        addFocusListener(object : FocusListener {
            override fun focusGained(e: FocusEvent?) {
                if (text.isNotEmpty()) {
                    selectionStart = 0
                    selectionEnd = text.length
                }
            }

            override fun focusLost(e: FocusEvent?) = Unit

        })
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        this.text.takeIf { it.isEmpty() }?.let {
            this.placeholder.takeIf { it.isNotEmpty() }?.let { t ->
                g.color = Color.gray
                g.drawString(t, 5, height - 5)
            }
        }
    }
}
