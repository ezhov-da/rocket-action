package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import java.awt.MouseInfo
import javax.swing.JDialog
import javax.swing.JFrame

class IconsDialog(
    private val iconRepository: IconRepository,
) : JDialog() {
    private val iconSelectionPanel = IconSelectionPanel(iconRepository)

    init {
        title = "Select icon"

        add(iconSelectionPanel)
        defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        isAlwaysOnTop = true

        isResizable = false
        pack()
    }

    fun selectIcon(callback: (String) -> Unit) {
        isVisible = true
        location = (MouseInfo.getPointerInfo().location)
        iconSelectionPanel.setCallback {
            callback(it)
            dispose()
        }
    }
}
