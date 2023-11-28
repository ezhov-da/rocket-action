package ru.ezhov.rocket.action.application.configuration.ui.edit

import ru.ezhov.rocket.action.application.configuration.ui.HandlerPanel
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextField

class InfoPanel(
    private val availableHandlersRepository: AvailableHandlersRepository
) : JPanel() {
    private val textFieldInfo = JTextField().apply { isEditable = false }

    init {
        layout = BorderLayout()
        add(textFieldInfo, BorderLayout.NORTH)
    }

    fun refresh(type: String, rocketActionId: String) {
        textFieldInfo.text = "type: $type id: $rocketActionId"
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
            }
            ?: run {
                add(textFieldInfo, BorderLayout.NORTH)
            }
    }
}
