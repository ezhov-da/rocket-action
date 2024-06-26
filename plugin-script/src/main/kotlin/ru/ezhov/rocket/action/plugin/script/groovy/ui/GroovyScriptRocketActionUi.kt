package ru.ezhov.rocket.action.plugin.script.groovy.ui

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.script.groovy.application.GroovyScriptEngine
import ru.ezhov.rocket.action.plugin.script.ui.ScriptMenu
import java.awt.Component
import java.util.*
import javax.swing.Icon

class GroovyScriptRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun info(): RocketActionPluginInfo = Properties().let { properties ->
        properties.load(this.javaClass.getResourceAsStream("/config/plugin-script.properties"))
        object : RocketActionPluginInfo {
            override fun version(): String = properties.getProperty("version")

            override fun author(): String = properties.getProperty("author")

            override fun link(): String? = properties.getProperty("link")
        }
    }

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[SCRIPT]
            ?.takeIf { it.isNotEmpty() }
            ?.let { script ->
                settings.settings()[LABEL]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { label ->
                        val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: script
                        val menu = ScriptMenu(
                            label = label,
                            script = script,
                            description = description,
                            executeOnLoad = settings.settings()[EXECUTE_ON_LOAD].toBoolean(),
                            context = context,
                            scriptEngine = GroovyScriptEngine(),
                        )

                        context.search().register(settings.id(), label)
                        context.search().register(settings.id(), description)

                        object : RocketAction, RocketActionHandlerFactory {
                            override fun contains(search: String): Boolean =
                                label.contains(search, ignoreCase = true)
                                    .or(description.contains(search, ignoreCase = true))

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                            override fun component(): Component = menu

                            override fun handler(): RocketActionHandler =
                                object : RocketActionHandler {
                                    override fun id(): String = settings.id()

                                    override fun contracts(): List<RocketActionHandlerCommandContract> =
                                        listOf(
                                            object : RocketActionHandlerCommandContract {
                                                override fun commandName(): String = "executeScript"

                                                override fun title(): String = label

                                                override fun description(): String =
                                                    "Executing the script described in the action"

                                                override fun inputArguments(): List<RocketActionHandlerProperty> =
                                                    emptyList()

                                                override fun outputParams(): List<RocketActionHandlerProperty> =
                                                    emptyList()

                                            }
                                        )

                                    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
                                        menu.executeScript()

                                        return RocketActionHandleStatus.Success()
                                    }
                                }
                        }
                    }
            }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Executing a Groovy Script"

    override fun asString(): List<String> = listOf(LABEL, SCRIPT)

    override fun properties(): List<RocketActionConfigurationProperty> =
        listOf(
            createRocketActionProperty(
                key = SCRIPT,
                name = SCRIPT,
                description = "Script to execute",
                required = true
            ),
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Header", required = true),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION,
                description = "Description",
                required = false
            ),
            createRocketActionProperty(
                key = EXECUTE_ON_LOAD,
                name = EXECUTE_ON_LOAD,
                description = "Execute script on boot",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = false),
            ),
        )

    override fun name(): String = "Groovy script"

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.BOLT)

    companion object {
        internal const val TYPE = "GROOVY_SCRIPT"
        internal val LABEL = "label"
        internal val SCRIPT = "script"
        internal val DESCRIPTION = "description"
        internal val EXECUTE_ON_LOAD = "executeOnLoad"
    }
}
