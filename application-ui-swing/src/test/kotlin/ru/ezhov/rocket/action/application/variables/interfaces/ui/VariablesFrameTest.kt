package ru.ezhov.rocket.action.application.variables.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.variables.application.VariableDto
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities


fun main() {
    val variablesDto = VariablesDto(
        key = "1213",
        variables = mutableListOf(
            VariableDto(
                name = "text name",
                value = "text value",
                description = "test description",
                type = VariableType.APPLICATION,
            )
        )
    )

    SwingUtilities.invokeLater {
        val frame = VariablesFrame(
            parent = null,
            variablesApplication = mockk {
                every { all() } returns variablesDto
            },
            notificationService = mockk {},
            iconService = mockk { every { by(any()) } returns ImageIcon(this::class.java.getResource("/icons/rocket_16x16.png")) },
        )

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}

// для обнаружения тестового класса
internal class VariablesFrameTest
