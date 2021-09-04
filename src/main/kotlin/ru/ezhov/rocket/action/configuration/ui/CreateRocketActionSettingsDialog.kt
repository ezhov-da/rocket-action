package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dialog
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.util.function.Consumer
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenuBar
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class CreateRocketActionSettingsDialog(
        owner: Dialog,
        private val rocketActionConfigurationRepository: RocketActionConfigurationRepository,
        private val rocketActionUiRepository: RocketActionUiRepository
) {
    private val comboBoxModel = DefaultComboBoxModel<RocketActionConfiguration>()
    private var comboBox: JComboBox<RocketActionConfiguration> = JComboBox(comboBoxModel)
    private val actionSettingsPanel: RocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentCallback: CreatedRocketActionSettingsCallback? = null
    private val testPanel: TestPanel = TestPanel()

    private val dialog: JDialog = JDialog(owner, "Create rocket action").apply {
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
        val all = rocketActionConfigurationRepository.all()
        val sortedAll = all.sortedBy { it.name() }
        sortedAll.forEach(Consumer { anObject: RocketActionConfiguration? -> comboBoxModel.addElement(anObject) })
        val panel = JPanel(BorderLayout())
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
        val buttonCreate = JButton("Create")
        panelCreateButton.add(buttonCreate)
        buttonCreate.addActionListener { e: ActionEvent? ->
            val settings = actionSettingsPanel.create()
            currentCallback!!.create(settings)
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

    private inner class TestPanel : JPanel(BorderLayout()) {
        private var panelTest: JPanel? = null
        fun createTest(settings: RocketActionSettings) {
            val panel: JPanel =
                    when (val actionUi = rocketActionUiRepository.by(settings.type())) {
                        null -> {
                            val p = JPanel(BorderLayout())
                            p.add(JLabel("Not found rocket action for type '" + settings.type() + "'"))
                            p
                        }
                        else -> {
                            val p = JPanel(BorderLayout())
                            val menuBar = JMenuBar()
                            val component = actionUi.create(settings).component()
                            menuBar.add(component)
                            p.add(menuBar, BorderLayout.CENTER)
                            p

                        }
                    }
            if (panelTest != null) {
                clearTest()
            }
            panelTest = panel
            add(panel, BorderLayout.CENTER)
            revalidate()
            repaint()
        }

        fun clearTest() {
            if (panelTest != null) {
                this.remove(panelTest)
                revalidate()
                this.repaint()
            }
        }

        init {
            val panel = JPanel()
            val buttonTest = JButton("Test")
            buttonTest.addActionListener { SwingUtilities.invokeLater { createTest(actionSettingsPanel.create()) } }
            panel.add(buttonTest)
            add(panel, BorderLayout.SOUTH)
        }
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
                            compareByDescending<RocketActionConfigurationProperty> { it.isRequired }
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

        fun create(): RocketActionSettings = NewRocketActionSettings(
                currentConfiguration!!.type(),
                settingPanels.associate { panel -> panel.value() }
        )
    }

    private class SettingPanel(private val property: RocketActionConfigurationProperty) : JPanel() {
        private val value = JTextPane()

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
            if (property.isRequired) {
                topPanel.add(JLabel("*").apply { foreground = Color.RED })
            }
            topPanel.add(labelDescription)
            val centerPanel = JPanel(BorderLayout())
            centerPanel.add(JScrollPane(value), BorderLayout.CENTER)
            this.add(topPanel, BorderLayout.NORTH)
            this.add(centerPanel, BorderLayout.CENTER)
        }

        fun value(): Pair<String, String> = Pair(first = property.key(), value.text)
    }
}