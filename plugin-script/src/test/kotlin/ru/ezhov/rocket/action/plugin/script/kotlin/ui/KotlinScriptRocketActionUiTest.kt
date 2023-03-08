package ru.ezhov.rocket.action.plugin.script.kotlin.ui

import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.variables.VariablesService
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
        val factory = ui.factory(object : RocketActionContext {
            override fun icon(): IconService {
                TODO("Not yet implemented")
            }

            override fun notification(): NotificationService {
                TODO("Not yet implemented")
            }

            override fun cache(): CacheService {
                TODO("Not yet implemented")
            }

            override fun variables(): VariablesService {
                TODO("Not yet implemented")
            }

        })
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
            },
            context = object : RocketActionContext {
                override fun icon(): IconService {
                    TODO("Not yet implemented")
                }

                override fun notification(): NotificationService {
                    TODO("Not yet implemented")
                }

                override fun cache(): CacheService {
                    TODO("Not yet implemented")
                }

                override fun variables(): VariablesService = object : VariablesService {
                    override fun variables(): Map<String, String> = emptyMap()
                }

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

internal class KotlinScriptRocketActionUiTest
