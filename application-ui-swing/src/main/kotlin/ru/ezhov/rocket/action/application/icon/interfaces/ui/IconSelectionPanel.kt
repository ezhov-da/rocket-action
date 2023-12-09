package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import java.awt.BorderLayout
import javax.swing.JPanel

class IconSelectionPanel(
    private val iconRepository: IconRepository,
) : JPanel(BorderLayout()) {
    private val iconsPanel: IconsPanel = IconsPanel(iconRepository)

    init {
        add(iconsPanel, BorderLayout.CENTER)
    }

    fun setCallback(callback: (String) -> Unit) {
        iconsPanel.setCallback(callback)
    }
}
