package ru.ezhov.rocket.action.application.configuration.ui

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.tags.ui.TagsPanelFactory
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ItemEvent
import java.util.function.Consumer
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants


class CreateRocketActionSettingsDialog(
    owner: JFrame,
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
) {
    private val comboBoxModel = DefaultComboBoxModel<RocketActionConfiguration>()
    private var comboBox: JComboBox<RocketActionConfiguration> = JComboBox(comboBoxModel)
    private val actionSettingsPanel: RocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentCallback: CreatedRocketActionSettingsCallback? = null
    private val testPanel: TestPanel =
        TestPanel(rocketActionPluginApplicationService = rocketActionPluginApplicationService) {
            actionSettingsPanel.create().second
        }

    private val dialog: JDialog = JDialog(owner, "Создать действие").apply {
        this.setIconImage(RocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage())
        val ownerSize = owner.size
        setSize((ownerSize.width * 0.7).toInt(), (ownerSize.height * 0.7).toInt())

        add(panelComboBox(), BorderLayout.NORTH)

        actionSettingsPanel.setRocketActionConfiguration(
            comboBox.selectedItem as RocketActionConfiguration
        )

        add(JScrollPane(actionSettingsPanel), BorderLayout.CENTER)

        add(createTestAndSaveDialog(), BorderLayout.SOUTH)
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        setLocationRelativeTo(owner)
    }

    private fun panelComboBox(): JPanel {
        val all = rocketActionPluginApplicationService.all().map { it.configuration(RocketActionContextFactory.context) }
        val sortedAll = all.sortedBy { it.name() }
        sortedAll.forEach(Consumer { anObject: RocketActionConfiguration? -> comboBoxModel.addElement(anObject) })
        val panel = JPanel(BorderLayout())
        comboBox.maximumRowCount = 20
        comboBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                value.let {
                    val configuration = value as RocketActionConfiguration
                    label.text = configuration.name()
                    label.toolTipText = configuration.description()
                }
                return label
            }
        }
        comboBox.addItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater {
                    actionSettingsPanel.setRocketActionConfiguration(e.item as RocketActionConfiguration)
                    testPanel.clearTest()
                }
            }
        }
        panel.add(comboBox, BorderLayout.CENTER)
        return panel
    }

    private fun createTestAndSaveDialog(): JPanel {
        val panel = JPanel(BorderLayout())
        val panelCreateButton = JPanel()
        val buttonCreate = JButton("Создать")
        panelCreateButton.add(buttonCreate)
        buttonCreate.addActionListener {
            val settings = actionSettingsPanel.create()
            currentCallback!!.create(
                TreeRocketActionSettings(
                    configuration = settings.first,
                    settings = settings.second
                )
            )
            dialog.isVisible = false
        }
        panel.add(testPanel, BorderLayout.NORTH)
        panel.add(panelCreateButton, BorderLayout.CENTER)
        return panel
    }

    fun show(callback: CreatedRocketActionSettingsCallback?) {
        currentCallback = callback
        dialog.isVisible = true
    }

    private inner class RocketActionSettingsPanel : JPanel() {
        private val settingPanels = mutableListOf<SettingPanel>()
        private var currentConfiguration: RocketActionConfiguration? = null
        private val tagsPanel = TagsPanelFactory.panel()

        init {
            this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        fun setRocketActionConfiguration(configuration: RocketActionConfiguration) {
            removeAll()
            settingPanels.clear()
            this.currentConfiguration = configuration
            configuration
                .properties()
                .sortedWith(
                    compareByDescending<RocketActionConfigurationProperty> { it.isRequired() }
                        .thenBy { it.name() }
                )
                .forEach { p: RocketActionConfigurationProperty ->
                    val panel = SettingPanel(p)
                    this.add(panel)
                    settingPanels.add(panel)
                }
            tagsPanel.border = BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createTitledBorder("Tags")
            )
            this.add(tagsPanel)
            this.add(Box.createVerticalBox())
            repaint()
            revalidate()
        }

        fun create() =
            Pair(
                first = this.currentConfiguration!!,
                second = MutableRocketActionSettings(
                    id = RocketActionSettingsModel.generateId(),
                    type = currentConfiguration!!.type().value(),
                    settings = settingPanels.map { panel -> panel.value() }.toMutableList(),
                    tags = tagsPanel.tags(),
                )
            )
    }

    private class SettingPanel(private val property: RocketActionConfigurationProperty) : JPanel() {
        private val valueCallback: () -> Pair<String, SettingsValueType?>

        init {
            this.layout = BorderLayout()
            this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
            val labelDescription = JLabel(RocketActionContextFactory.context.icon().by(AppIcon.INFO))
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

            val centerPanel = JPanel(BorderLayout())
            when (val configProperty = property.property()) {
                is RocketActionPropertySpec.StringPropertySpec -> {
                    val plainText = JRadioButton("Простой текст").apply { isSelected = true }
                    val mustacheTemplate = JRadioButton("Шаблон Mustache")
                    val groovyTemplate = JRadioButton("Шаблон Groovy")
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
                                    tp.text = configProperty.defaultValue.orEmpty()
                                }
                        ),
                        BorderLayout.CENTER
                    )
                }

                is RocketActionPropertySpec.BooleanPropertySpec -> {
                    centerPanel.add(
                        JScrollPane(
                            JCheckBox()
                                .also { cb ->
                                    cb.isSelected = configProperty.defaultValue.toBoolean()
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
                    list.selectedItem = default
                    centerPanel.add(JScrollPane(
                        list
                            .also { l ->
                                valueCallback = { Pair(l.selectedItem.toString(), null) }
                            }
                    ), BorderLayout.CENTER)
                }

                is RocketActionPropertySpec.IntPropertySpec -> {
                    val default = configProperty.defaultValue?.toIntOrNull() ?: 0
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

        fun value(): SettingsModel =
            SettingsModel(
                name = property.key(),
                value = valueCallback().first,
                valueType = valueCallback().second
            )
    }
}
