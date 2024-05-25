package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.BooleanPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ComponentPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.IntPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ListPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.StringPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ValuePanel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.ui.utils.swing.MarkdownEditorPane
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

class SettingPanel(
    private val property: RocketActionConfigurationProperty
) : JPanel() {
    private val centerPanel: ValuePanel
    private val labelText: String

    init {
        this.layout = BorderLayout()
        this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)


        labelText = if (property.isRequired()) {
            """<html><p>${property.name()} <font color="red">*</font></p>"""
        } else {
            """<html><p>${property.name()}</p>"""
        }

        centerPanel =
            when (val configProperty = property.property()) {
                is RocketActionPropertySpec.StringPropertySpec -> StringPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.BooleanPropertySpec -> BooleanPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.ListPropertySpec -> ListPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.IntPropertySpec -> IntPropertySpecPanel(configProperty)

                is RocketActionPropertySpec.ComponentPropertySpec -> ComponentPropertySpecPanel(configProperty)
            }

        val tabs = JTabbedPane()
        tabs.addTab("Configuration", centerPanel)
        tabs.addTab("Info", JScrollPane(MarkdownEditorPane.fromText(property.description())))

        this.add(tabs, BorderLayout.CENTER)
    }

    fun labelText(): String = labelText

    fun value(): SettingsModel = centerPanel.value().let { value ->
        SettingsModel(
            name = property.key(),
            value = value.value,
            valueType = value.type
        )
    }
}
