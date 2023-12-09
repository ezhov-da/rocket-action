package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.toIcon
import java.awt.BorderLayout
import javax.swing.Icon
import javax.swing.JButton
import javax.swing.JPanel

class SelectIconPanel(
    private val iconRepository: IconRepository,
    private val initIcon: Icon? = null,
) : JPanel(BorderLayout()) {
    private val selectButton = JButton().apply { toolTipText = "Select icon" }
    private val resetButton = JButton(Icons.Standard.X_16x16).apply { toolTipText = "Reset icon" }
    private val dialog = IconsDialog(iconRepository)

    private var selectedIcon: String? = null

    init {
        initIcon?.let {
            selectButton.icon = initIcon
        } ?: run {
            selectButton.icon = Icons.Advanced.IMAGE_16x16
        }

        selectButton.addActionListener {
            dialog.selectIcon {
                selectedIcon = it
                selectButton.icon = it.toIcon()
            }
        }

        resetButton.addActionListener {
            resetIcon()
        }

        add(selectButton, BorderLayout.CENTER)
        add(resetButton, BorderLayout.EAST)
    }

    private fun resetIcon() {
        selectButton.icon = Icons.Advanced.IMAGE_16x16
        selectedIcon = null
    }

    fun setIcon(icon: String?) {
        icon?.let {
            selectedIcon = it
            selectButton.icon = it.toIcon()
        } ?: run {
            resetIcon()
        }
    }

    fun selectedIcon(): String? = selectedIcon
}
