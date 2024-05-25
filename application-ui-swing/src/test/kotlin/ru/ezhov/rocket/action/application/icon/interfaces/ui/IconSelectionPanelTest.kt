package ru.ezhov.rocket.action.application.icon.interfaces.ui

import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class IconSelectionPanelTest

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }

        IconsDialog(IconRepository(TestUtilsFactory.objectMapper)).selectIcon {
            println(it)
        }
    }
}
