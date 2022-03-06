package ru.ezhov.rocket.action.types.template

import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Cursor
import java.awt.Desktop
import java.awt.GridLayout
import java.awt.event.KeyListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import java.util.stream.Collectors
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

internal class PanelEngine(words: List<String>, keyListenerForAllTextFields: KeyListener?) : JPanel() {
    private val words: List<String>
    private val map: MutableMap<String?, JTextField> = HashMap()
    fun apply(): Map<String, String> {
        return map
            .entries
            .stream()
            .collect(
                Collectors.toMap<Map.Entry<String?, JTextField>, String, String>(
                    { (key): Map.Entry<String?, JTextField> -> key!!.replace("\\$".toRegex(), "") },
                    { (key, value): Map.Entry<String?, JTextField> ->
                        if ("" == value.text) {
                            key
                        } else {
                            value.text
                        }
                    }
                )
            )
    }

    fun initVariables(map: Map<String, String?>) {
        map.forEach { (k: String, v: String?) ->
            val textField = this.map["$$k"]
            if (textField != null) {
                textField.text = v
            }
        }
    }

    init {
        this.words = words.sortedBy { it }
        layout = BorderLayout()
        val panelTextField = JPanel(GridLayout(words.size, 1))
        panelTextField.layout = GridLayout(words.size, 1)
        for (s in this.words) {
            val textField: JTextField = TextFieldWithText(s)
            map[s] = textField
            panelTextField.add(textField)
            keyListenerForAllTextFields?.let {
                textField.addKeyListener(keyListenerForAllTextFields)
            }
        }
        val label = JLabel("https://velocity.apache.org/engine/1.7/user-guide.html")
        val foregroundDefault = label.foreground
        val cursorDefault = label.cursor
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseReleased(e: MouseEvent) {
                try {
                    Desktop.getDesktop().browse(URI(label.text))
                } catch (ioException: Exception) {
                    //no matter
                }
            }

            override fun mouseEntered(e: MouseEvent) {
                label.foreground = Color.BLUE
                label.cursor = Cursor(Cursor.HAND_CURSOR)
            }

            override fun mouseExited(e: MouseEvent) {
                label.foreground = foregroundDefault
                label.cursor = cursorDefault
            }
        })
        add(label, BorderLayout.NORTH)
        add(panelTextField, BorderLayout.CENTER)
    }
}