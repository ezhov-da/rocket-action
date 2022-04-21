package ru.ezhov.rocket.action.plugin.script.kotlin.ui

import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import java.awt.BorderLayout
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.SwingUtilities
import javax.swing.UIManager

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
        }

        val ui: RocketActionPlugin = KotlinScriptRocketActionUi()
        val factory = ui.factory()
        val rocketAction = factory.create(
            object : RocketActionSettings {
                override fun id(): String = "123"

                override fun type(): RocketActionType = RocketActionType { KotlinScriptRocketActionUi.TYPE }

                override fun settings(): Map<RocketActionConfigurationPropertyKey, String> =
                    mapOf(
                        KotlinScriptRocketActionUi.LABEL to "TEST",
                        KotlinScriptRocketActionUi.SCRIPT to "\"test\"",
                        KotlinScriptRocketActionUi.DESCRIPTION to "Description",
                        KotlinScriptRocketActionUi.EXECUTE_ON_LOAD to "true",
                    )

                override fun actions(): List<RocketActionSettings> = emptyList()
            }
        )
        val component = rocketAction!!.component()

        JFrame().apply {
            val menu = JMenuBar()
            menu.add(component)
            add(menu, BorderLayout.NORTH)
            pack()
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            isVisible = true
        }
    }
}
