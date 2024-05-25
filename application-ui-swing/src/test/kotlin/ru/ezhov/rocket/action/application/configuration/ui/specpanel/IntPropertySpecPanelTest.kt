package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import javax.swing.JFrame
import javax.swing.UIManager

internal class IntPropertySpecPanelTest


fun main() {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (ex: Throwable) {
        //
    }
    val frame = JFrame("_________")

    frame.add(
        IntPropertySpecPanel(
            configProperty = RocketActionPropertySpec.IntPropertySpec(12, 1, 1000),
            initValue = InitValue(
                value = "1",
                property = mockk {
                    every { isRequired() } returns true
                },
                type = SettingsValueType.GROOVY_TEMPLATE
            ),
        )
    )
    frame.setSize(1000, 600)
    frame.setLocationRelativeTo(null)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.isVisible = true
}


