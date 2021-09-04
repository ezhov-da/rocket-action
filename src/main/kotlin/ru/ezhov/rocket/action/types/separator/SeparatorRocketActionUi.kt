package ru.ezhov.rocket.action.types.separator

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.types.AbstractRocketAction
import java.awt.Component
import javax.swing.JSeparator

class SeparatorRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Action =
            object : Action {
                override fun action(): SearchableAction = object : SearchableAction {
                    override fun contains(search: String): Boolean = false
                }

                override fun component(): Component = JSeparator()
            }

    override fun type(): String = "SEPARATOR"

    override fun name(): String = "Разделитель"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> = emptyList()
}