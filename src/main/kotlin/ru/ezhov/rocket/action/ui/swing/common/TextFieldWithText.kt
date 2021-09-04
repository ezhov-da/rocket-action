package ru.ezhov.rocket.action.ui.swing.common

import java.awt.Color
import java.awt.Graphics
import javax.swing.JTextField

class TextFieldWithText(private val placeholder: String) : JTextField() {
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