package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane

// TODO ezhov
class IconSelectionPanel(
    private val iconRepository: IconRepository
) : JPanel(BorderLayout()) {
    private val iconsPanel: IconsPanel = IconsPanel(iconRepository)

    init {
        add(JScrollPane(iconsPanel), BorderLayout.CENTER)
    }
}
