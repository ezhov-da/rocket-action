package ru.ezhov.rocket.action.application.handlers.apikey.interfaces.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeyDto
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysDto
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities

// для обнаружения тестового класса
class ApiKeysFrameTest

fun main() {
    val apiKeysDto = ApiKeysDto(
        keys = mutableListOf(
            ApiKeyDto(
                value = "text value",
                description = "test description",
            )
        )
    )

    SwingUtilities.invokeLater {
        val frame = ApiKeysFrame(
            parent = null,
            apiKeysApplication = mockk {
                every { all() } returns apiKeysDto
            },
            notificationService = mockk {},
            iconService = mockk { every { by(any()) } returns ImageIcon(this::class.java.getResource("/icons/rocket_16x16.png")) },
        )

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isVisible = true
    }
}
