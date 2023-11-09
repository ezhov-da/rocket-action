package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.BooleanPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.IntPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ListPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.StringPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ValuePanel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class SettingPanel(
    rocketActionContextFactory: RocketActionContextFactory,
    private val property: RocketActionConfigurationProperty
) : JPanel() {
    private val centerPanel: ValuePanel

    init {
        this.layout = BorderLayout()
        this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
        val labelDescription = JLabel(rocketActionContextFactory.context.icon().by(AppIcon.INFO))
        labelDescription.toolTipText = property.description()

        val text = if (property.isRequired()) {
            """<html><p>${property.name()} <font color="red">*</font></p>"""
        } else {
            """<html><p>${property.name()}</p>"""
        }
        border = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder(text)
        )

        val topPanel = JPanel()
        topPanel.layout = BoxLayout(topPanel, BoxLayout.X_AXIS)
        topPanel.border = BorderFactory.createEmptyBorder(0, 0, 1, 0)
        topPanel.add(labelDescription)

        centerPanel =
            when (val configProperty = property.property()) {
                is RocketActionPropertySpec.StringPropertySpec -> StringPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.BooleanPropertySpec -> BooleanPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.ListPropertySpec -> ListPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.IntPropertySpec -> IntPropertySpecPanel(configProperty)
            }

        this.add(topPanel, BorderLayout.NORTH)
        this.add(centerPanel, BorderLayout.CENTER)
    }

    fun value(): SettingsModel = centerPanel.value().let { value ->
        SettingsModel(
            name = property.key(),
            value = value.value,
            valueType = value.type
        )
    }
}
