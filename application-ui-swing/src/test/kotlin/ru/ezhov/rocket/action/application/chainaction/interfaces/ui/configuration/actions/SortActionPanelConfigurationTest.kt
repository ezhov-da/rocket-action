package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions

import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class SortActionPanelConfigurationTest

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ex: Throwable) {
            //
        }
        val frame = JFrame("_________");
        frame.add(
            SortActionPanelConfiguration().apply {
                addPropertyChangeListener {
                    if (it.propertyName == SortActionPanelConfiguration.SORT_INFO_PROPERTY_NAME) {
                        println(it.newValue)
                    }
                }
            }
        )
        frame.setSize(500, 300)
        frame.setLocationRelativeTo(null);
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE;
        frame.isVisible = true;
    }
}
