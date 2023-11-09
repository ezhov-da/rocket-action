package ru.ezhov.rocket.action.application.configuration.ui.edit

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.BooleanPropertySpecPanel
import ru.ezhov.rocket.action.application.configuration.ui.specpanel.InitValue
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

private val logger = KotlinLogging.logger {}

/**
 * Panel with one setting
 */
class SettingPanel(
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val value: Value
) : JPanel(BorderLayout()) {
    private var centerPanel: ValuePanel? = null

    init {
        value.property
            ?.let { property ->
                val text = if (property.isRequired()) {
                    """<html><p>${property.name()} <font color="red">*</font></p>"""
                } else {
                    """<html><p>${property.name()}</p>"""
                }
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    BorderFactory.createTitledBorder(text)
                )

                val labelDescription = JLabel(rocketActionContextFactory.context.icon().by(AppIcon.INFO))
                labelDescription.toolTipText = property.description()

                val topPanel = JPanel()
                topPanel.layout = BoxLayout(topPanel, BoxLayout.X_AXIS)
                topPanel.border = BorderFactory.createEmptyBorder(0, 0, 1, 0)
                topPanel.add(labelDescription)

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

                this.add(labelDescription, BorderLayout.NORTH)
                this.add(centerPanel, BorderLayout.CENTER)
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

    fun value(): SettingsModel = centerPanel!!.value().let { valPanel ->
        SettingsModel(
            name = value.key,
            value = valPanel.value,
            valueType = valPanel.type
        )
    }
}
