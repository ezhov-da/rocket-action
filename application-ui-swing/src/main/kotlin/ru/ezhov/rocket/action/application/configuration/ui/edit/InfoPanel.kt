package ru.ezhov.rocket.action.application.configuration.ui.edit

import ru.ezhov.rocket.action.application.configuration.ui.HandlerPanel
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class InfoPanel(
    private val availableHandlersRepository: AvailableHandlersRepository
) : JPanel() {
    private val textFieldInfo = JTextField().apply { isEditable = false }
    private val labelDescription = JLabel()

    init {
        layout = BorderLayout()
        add(textFieldInfo, BorderLayout.NORTH)
        add(labelDescription, BorderLayout.CENTER)
    }

    fun refresh(type: String, rocketActionId: String, description: String?) {
        textFieldInfo.text = "type: $type id: $rocketActionId"
        description?.let {
            labelDescription.text = description
        }
        removeAll()
        HandlerPanel.of(rocketActionId, availableHandlersRepository)
            ?.let { hp ->
                add(
                    JPanel(BorderLayout()).apply {
                        add(textFieldInfo, BorderLayout.CENTER)
                        add(hp, BorderLayout.EAST)
                    },
                    BorderLayout.NORTH
                )
                add(labelDescription, BorderLayout.CENTER)
            }
            ?: run {
                add(textFieldInfo, BorderLayout.NORTH)
                add(labelDescription, BorderLayout.CENTER)
            }
    }
}
