package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.api.PropertyType
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.toImage
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.event.ItemEvent
import java.util.function.Consumer
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
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
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class CreateRocketActionSettingsDialog(
    owner: JFrame,
    private val rocketActionPluginRepository: RocketActionPluginRepository,
) {
    private val comboBoxModel = DefaultComboBoxModel<RocketActionConfiguration>()
    private var comboBox: JComboBox<RocketActionConfiguration> = JComboBox(comboBoxModel)
    private val actionSettingsPanel: RocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentCallback: CreatedRocketActionSettingsCallback? = null
    private val testPanel: TestPanel =
        TestPanel(rocketActionPluginRepository = rocketActionPluginRepository) {
            actionSettingsPanel.create()
        }

    private val dialog: JDialog = JDialog(owner, "Создать действие").apply {
        this.setIconImage(IconRepositoryFactory.repository.by(AppIcon.ROCKET_APP).toImage())
        val ownerSize = owner.size
        setSize((ownerSize.width * 0.7).toInt(), (ownerSize.height * 0.7).toInt())
        add(panelComboBox(), BorderLayout.NORTH)
        actionSettingsPanel.setRocketActionConfiguration(comboBox.selectedItem as RocketActionConfiguration)
        add(JScrollPane(actionSettingsPanel), BorderLayout.CENTER)
        add(createTestAndSaveDialog(), BorderLayout.SOUTH)
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        setLocationRelativeTo(owner)
    }

    @Throws(Exception::class)
    private fun panelComboBox(): JPanel {
        val all = rocketActionPluginRepository.all().map { it.configuration() }
        val sortedAll = all.sortedBy { it.name() }
        sortedAll.forEach(Consumer { anObject: RocketActionConfiguration? -> comboBoxModel.addElement(anObject) })
        val panel = JPanel(BorderLayout())
        comboBox.maximumRowCount = 20
        comboBox.renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
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
                    configuration = settings.configuration,
                    settings = MutableRocketActionSettings(
                        settings.id(),
                        settings.type(),
                        settings.settings().toMutableMap(),
                        settings.actions().toMutableList()
                    )
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
            this.add(Box.createVerticalBox())
            repaint()
            revalidate()
        }

        fun create(): NewRocketActionSettings = NewRocketActionSettings(
            configuration = this.currentConfiguration!!,
            type = currentConfiguration!!.type(),
            settings = settingPanels.associate { panel -> panel.value() }
        )
    }

    private class SettingPanel(private val property: RocketActionConfigurationProperty) : JPanel() {
        private val valueCallback: () -> String

        init {
            this.layout = BorderLayout()
            this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
            val labelName = JLabel(property.name())
            val labelDescription = JLabel(IconRepositoryFactory.repository.by(AppIcon.INFO))
            labelDescription.toolTipText = property.description()

            val topPanel = JPanel()
            topPanel.layout = BoxLayout(topPanel, BoxLayout.X_AXIS)
            topPanel.border = BorderFactory.createEmptyBorder(0, 0, 1, 0)
            topPanel.add(labelName)
            if (property.isRequired()) {
                topPanel.add(JLabel("*").apply { foreground = Color.RED })
            }
            topPanel.add(labelDescription)

            val centerPanel = JPanel(BorderLayout())
            when (property.type()) {
                PropertyType.STRING -> {
                    centerPanel.add(
                        JScrollPane(
                            JTextPane().also { tp ->
                                valueCallback = { tp.text }
                                tp.text = property.default().orEmpty()
                            }
                        ),
                        BorderLayout.CENTER
                    )
                }
                PropertyType.BOOLEAN -> {
                    centerPanel.add(JScrollPane(
                        JCheckBox().also { cb ->
                            cb.isSelected = property.default().toBoolean()
                            valueCallback = { cb.isSelected.toString() }
                        }
                    ), BorderLayout.CENTER)
                }
            }

            this.add(topPanel, BorderLayout.NORTH)
            this.add(centerPanel, BorderLayout.CENTER)
        }

        fun value(): Pair<RocketActionConfigurationPropertyKey, String> = Pair(first = property.key(), second = valueCallback())
    }
}