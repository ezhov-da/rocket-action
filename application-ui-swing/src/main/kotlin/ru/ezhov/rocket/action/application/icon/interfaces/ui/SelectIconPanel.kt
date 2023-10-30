package ru.ezhov.rocket.action.application.icon.interfaces.ui

import java.awt.BorderLayout
import javax.swing.JPanel

class SelectIconPanel : JPanel(BorderLayout()) {
    private val iconsPanel: IconsPanel = IconsPanel()

    init {
        add(iconsPanel, BorderLayout.CENTER)
    }
}
