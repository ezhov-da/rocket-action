package ru.ezhov.rocket.action.plugin.separator

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import javax.swing.Icon
import javax.swing.JSeparator

class SeparatorRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null
    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction =
        JSeparator().let { sep ->
            object : RocketAction {
                override fun contains(search: String): Boolean = false

                override fun isChanged(actionSettings: RocketActionSettings): Boolean = false

                override fun component(): Component = sep
            }
        }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = emptyList()

    override fun type(): RocketActionType = RocketActionType { "SEPARATOR" }

    override fun name(): String = "Разделитель"

    override fun description(): String = "Разделитель"

    override fun icon(): Icon? = actionContext!!.icon().by(AppIcon.MINUS)

    override fun properties(): List<RocketActionConfigurationProperty> = emptyList()
}
