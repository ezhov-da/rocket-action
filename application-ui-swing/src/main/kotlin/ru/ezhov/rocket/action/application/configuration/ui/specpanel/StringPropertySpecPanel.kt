package ru.ezhov.rocket.action.application.configuration.ui.specpanel

import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import java.awt.BorderLayout
import java.awt.event.ActionListener
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

class StringPropertySpecPanel(
    private val configProperty: RocketActionPropertySpec.StringPropertySpec,
    initValue: InitValue? = null,
) : ValuePanel(BorderLayout()) {
    private var valueCallback: () -> SpecValue

    init {
        val plainText = JRadioButton("Plain text").apply { isSelected = true }
        val mustacheTemplate = JRadioButton("Mustache template")
        val groovyTemplate = JRadioButton("Groovy template")

        ButtonGroup().apply {
            add(plainText)
            add(mustacheTemplate)
            add(groovyTemplate)
        }

        initValue?.let {
            when (it.type) {
                SettingsValueType.PLAIN_TEXT -> plainText.isSelected = true
                SettingsValueType.MUSTACHE_TEMPLATE -> mustacheTemplate.isSelected = true
                SettingsValueType.GROOVY_TEMPLATE -> groovyTemplate.isSelected = true
                else -> plainText.isSelected = true
            }
        }

        add(JPanel(MigLayout()).apply {
            add(plainText)
            add(mustacheTemplate)
            add(groovyTemplate)
        }, BorderLayout.NORTH)

        val rSyntaxTextArea = RSyntaxTextArea()
            .also { tp ->
                when (groovyTemplate.isSelected) {
                    true -> tp.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_GROOVY
                    false -> tp.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE
                }
                valueCallback = {
                    SpecValue(
                        value = tp.text,
                        type = when {
                            plainText.isSelected -> SettingsValueType.PLAIN_TEXT
                            mustacheTemplate.isSelected -> SettingsValueType.MUSTACHE_TEMPLATE
                            groovyTemplate.isSelected -> SettingsValueType.GROOVY_TEMPLATE
                            else -> SettingsValueType.PLAIN_TEXT

                        }
                    )
                }
                initValue?.let {
                    if (it.property?.isRequired() == true && it.value.isEmpty()) {
                        tp.text = configProperty.defaultValue ?: ""
                    } else {
                        tp.text = it.value
                    }
                }
            }

        val actionListener = ActionListener {
            when (groovyTemplate.isSelected) {
                true -> rSyntaxTextArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_GROOVY
                false -> rSyntaxTextArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_NONE
            }
        }

        plainText.addActionListener(actionListener)
        mustacheTemplate.addActionListener(actionListener)
        groovyTemplate.addActionListener(actionListener)

        add(
            RTextScrollPane(rSyntaxTextArea),
            BorderLayout.CENTER
        )
    }

    override fun value(): SpecValue = valueCallback()
}
