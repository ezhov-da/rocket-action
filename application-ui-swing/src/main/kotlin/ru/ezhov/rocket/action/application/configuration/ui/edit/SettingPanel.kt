package ru.ezhov.rocket.action.application.configuration.ui.edit

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.BooleanPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.InitValue
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.IntPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ListPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.StringPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.ValuePanel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.ui.utils.swing.MarkdownEditorPane
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

private val logger = KotlinLogging.logger {}

/**
 * Panel with one setting
 */
class SettingPanel(
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val value: Value
) : JPanel(BorderLayout()) {
    private var centerPanel: ValuePanel? = null
    private var labelText: String = ""

    init {
        value.property
            ?.let { property ->
                labelText = if (property.isRequired()) {
                    """<html><p>${property.name()} <font color="red">*</font></p>"""
                } else {
                    """<html><p>${property.name()}</p>"""
                }

                centerPanel = when (val configProperty = property.property()) {
                    is RocketActionPropertySpec.StringPropertySpec -> StringPropertySpecPanel(
                        configProperty = configProperty,
                        initValue = InitValue(
                            value = value.value,
                            property = value.property,
                            type = value.valueType
                        )
                    )

                    is RocketActionPropertySpec.BooleanPropertySpec -> BooleanPropertySpecPanel(
                        configProperty = configProperty,
                        initValue = InitValue(
                            value = value.value,
                            property = value.property,
                            type = value.valueType
                        )
                    )

                    is RocketActionPropertySpec.ListPropertySpec -> ListPropertySpecPanel(
                        configProperty = configProperty,
                        initValue = InitValue(
                            value = value.value,
                            property = value.property,
                            type = value.valueType
                        )
                    )

                    is RocketActionPropertySpec.IntPropertySpec -> IntPropertySpecPanel(
                        configProperty = configProperty,
                        initValue = InitValue(
                            value = value.value,
                            property = value.property,
                            type = value.valueType
                        )
                    )
                }

                val tabs = JTabbedPane()
                tabs.addTab("Configuration", centerPanel)
                tabs.addTab("Info", JScrollPane(MarkdownEditorPane.fromText(property.description())))

                this.add(tabs, BorderLayout.CENTER)
            }
            ?: run {
                val text = "Unregistered property found '${value.key}:${value.value}' " +
                    "description=${value.property?.description()}"
                logger.warn { text }
                rocketActionContextFactory.context.notification().show(
                    type = NotificationType.WARN,
                    text = text
                )
            }
    }

    fun labelText(): String = labelText

    fun value(): SettingsModel = centerPanel!!.value().let { valPanel ->
        SettingsModel(
            name = value.key,
            value = valPanel.value,
            valueType = valPanel.type
        )
    }
}
