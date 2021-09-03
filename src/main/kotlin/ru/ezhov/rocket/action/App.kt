package ru.ezhov.rocket.action

import javax.swing.JDialog
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            //
        }
        var path: String? = null
        if (args.isNotEmpty()) {
            path = args[0]
        }
        try {
            val actionService = UiQuickActionService(path)
            JDialog().apply {
                jMenuBar = actionService.createMenu(this)
                isUndecorated = true
                pack()
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                isVisible = true
            }
        } catch (e: UiQuickActionServiceException) {
            e.printStackTrace()
        }
    }
}
