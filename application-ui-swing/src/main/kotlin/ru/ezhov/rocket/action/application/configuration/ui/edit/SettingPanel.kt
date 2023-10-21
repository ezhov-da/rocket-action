package ru.ezhov.rocket.action.application.configuration.ui.edit

import mu.KotlinLogging
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

private val logger = KotlinLogging.logger {}

/**
 * Panel with one setting
 */
class SettingPanel(
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val value: Value
) : JPanel() {
    private var valueCallback: () -> Pair<String, SettingsValueType?> = { Pair("", null) }

    init {
        this.layout = BorderLayout()
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

                val centerPanel = JPanel(BorderLayout())
                when (val configProperty = property.property()) {
                    is RocketActionPropertySpec.StringPropertySpec -> {
                        val plainText = JRadioButton("Plain text").apply { }
                        val mustacheTemplate = JRadioButton("Mustache template")
                        val groovyTemplate = JRadioButton("Groovy template")

                        when (value.valueType) {
                            SettingsValueType.PLAIN_TEXT -> plainText.isSelected = true
                            SettingsValueType.MUSTACHE_TEMPLATE -> mustacheTemplate.isSelected = true
                            SettingsValueType.GROOVY_TEMPLATE -> groovyTemplate.isSelected = true
                            else -> plainText.isSelected = true
                        }

                        ButtonGroup().apply {
                            add(plainText)
                            add(mustacheTemplate)
                            add(groovyTemplate)
                        }

                        centerPanel.add(JPanel().apply {
                            add(plainText)
                            add(mustacheTemplate)
                            add(groovyTemplate)
                        }, BorderLayout.NORTH)

                        centerPanel.add(
                            RTextScrollPane(
                                RSyntaxTextArea()
                                    .also { tp ->
                                        tp.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE
                                        valueCallback = {
                                            Pair(
                                                first = tp.text,
                                                second = when {
                                                    plainText.isSelected -> SettingsValueType.PLAIN_TEXT
                                                    mustacheTemplate.isSelected -> SettingsValueType.MUSTACHE_TEMPLATE
                                                    groovyTemplate.isSelected -> SettingsValueType.GROOVY_TEMPLATE
                                                    else -> SettingsValueType.PLAIN_TEXT
                                                }
                                            )
                                        }
                                        if (property.isRequired() && value.value.isEmpty()) {
                                            tp.text = configProperty.defaultValue ?: ""
                                        } else {
                                            tp.text = value.value
                                        }
                                    }
                            ),
                            BorderLayout.CENTER
                        )
                    }

                    is RocketActionPropertySpec.BooleanPropertySpec -> {
                        centerPanel.add(JScrollPane(
                            JCheckBox()
                                .also { cb ->
                                    cb.isSelected = value.value.toBoolean()
                                    valueCallback = { Pair(cb.isSelected.toString(), null) }
                                }
                        ), BorderLayout.CENTER)
                    }

                    is RocketActionPropertySpec.ListPropertySpec -> {
                        val default = configProperty.defaultValue.orEmpty()
                        val selectedValues = configProperty.valuesForSelect
                        if (!selectedValues.contains(default)) {
                            selectedValues.toMutableList().add(default)
                        }
                        val list = JComboBox(selectedValues.toTypedArray())
                        list.selectedItem =
                            if (selectedValues.contains(value.value)) {
                                value.value
                            } else {
                                default
                            }
                        centerPanel.add(JScrollPane(
                            list
                                .also { l ->
                                    valueCallback = { Pair(l.selectedItem.toString(), null) }
                                }
                        ), BorderLayout.CENTER)
                    }

                    is RocketActionPropertySpec.IntPropertySpec -> {
                        val default = value.value.toIntOrNull()
                            ?: configProperty.defaultValue?.toIntOrNull()
                            ?: 0
                        centerPanel.add(
                            JSpinner(SpinnerNumberModel(default, configProperty.min, configProperty.max, 1))
                                .also {
                                    valueCallback = { Pair(it.model.value.toString(), null) }
                                },
                            BorderLayout.CENTER
                        )
                    }
                }

                this.add(topPanel, BorderLayout.NORTH)
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

    fun value(): SettingsModel = SettingsModel(
        name = value.key,
        value = valueCallback().first,
        valueType = valueCallback().second

    )
}
