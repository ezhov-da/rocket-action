package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import io.mockk.every
import io.mockk.mockk
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.api.context.variables.VariablesService
import ru.ezhov.rocket.action.plugin.script.kotlin.ui.KotlinScriptRocketActionUi
import java.awt.BorderLayout
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal class DynamicScriptRocketActionUiTest

fun main() {
    SwingUtilities.invokeLater {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
        }

        val notificationServiceTestImpl = object : NotificationService {
            override fun show(type: NotificationType, text: String) {
                println("$type $text")
            }
        }

        val ui: RocketActionPlugin = DynamicScriptRocketActionUi()
        val factory = ui.factory(object : RocketActionContext {
            override fun icon(): IconService = mockk()

            override fun notification(): NotificationService = notificationServiceTestImpl

            override fun cache(): CacheService = mockk()

            override fun variables(): VariablesService = mockk()

            override fun search(): Search = mockk {
                every { register(any(), any()) } returns Unit
            }
        })
        val rocketAction = factory.create(object : RocketActionSettings {
            override fun id(): String = "123"

            override fun type(): RocketActionType = RocketActionType { KotlinScriptRocketActionUi.TYPE }

            override fun settings(): Map<String, String> = mapOf(
                DynamicScriptRocketActionUi.LABEL to "TEST",
                DynamicScriptRocketActionUi.SCRIPT to "\"\$_v1 \$_v2\"",
                DynamicScriptRocketActionUi.DESCRIPTION to "Description",
                DynamicScriptRocketActionUi.COUNT_VARIABLES to "2",
                DynamicScriptRocketActionUi.SELECTED_SCRIPT_LANG to "GROOVY",
                DynamicScriptRocketActionUi.FIELD_NAMES to "First field: And default value\nSecond field",
                DynamicScriptRocketActionUi.INSTRUCTION to "First row\nSecond row",
            )

            override fun actions(): List<RocketActionSettings> = emptyList()
        }, context = object : RocketActionContext {
            override fun icon(): IconService = mockk {
                every { by(any()) } returns ImageIcon()
            }

            override fun notification(): NotificationService = notificationServiceTestImpl

            override fun cache(): CacheService = mockk()

            override fun variables(): VariablesService = object : VariablesService {
                override fun variables(): Map<String, String> = emptyMap()
            }

            override fun search(): Search = mockk {
                every { register(any(), any()) } returns Unit
            }
        })
        val component = rocketAction!!.component()

        JFrame().apply {
            val menu = JMenuBar()
            menu.add(component)
            add(menu, BorderLayout.NORTH)
            setLocationRelativeTo(null)
            defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            setSize(200, 200)
            isVisible = true
        }
    }
}
