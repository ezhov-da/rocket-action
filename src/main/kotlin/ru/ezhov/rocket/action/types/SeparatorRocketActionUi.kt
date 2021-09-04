package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import java.awt.Component
import javax.swing.JSeparator

class SeparatorRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component = JSeparator()

    override fun type(): String = "SEPARATOR"

    override fun name(): String = "Разделитель"

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> = emptyList()
}