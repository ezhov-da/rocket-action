package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.RocketActionUiRepository
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dialog
import java.awt.event.ActionEvent
import java.awt.event.ItemEvent
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.swing.*
import javax.swing.table.DefaultTableModel

class CreateRocketActionSettingsDialog(
        owner: Dialog,
        rocketActionConfigurationRepository: RocketActionConfigurationRepository,
        rocketActionUiRepository: RocketActionUiRepository
) {
    private val dialog: JDialog
    private val rocketActionConfigurationRepository: RocketActionConfigurationRepository
    private var comboBox: JComboBox<RocketActionConfiguration?>? = null
    private val actionSettingsPanel: RocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentCallback: CreatedRocketActionSettingsCallback? = null
    private val rocketActionUiRepository: RocketActionUiRepository
    private val testPanel: TestPanel = TestPanel()

    @Throws(Exception::class)
    private fun panelComboBox(): JPanel {
        val comboBoxModel = DefaultComboBoxModel<RocketActionConfiguration?>()
        val all = rocketActionConfigurationRepository.all()
        val sortedAll = all
                .stream()
                .sorted(Comparator.comparing { obj: RocketActionConfiguration? -> obj!!.type() })
                .collect(Collectors.toList())
        sortedAll.forEach(Consumer { anObject: RocketActionConfiguration? -> comboBoxModel.addElement(anObject) })
        val panel = JPanel(BorderLayout())
        comboBox = JComboBox(comboBoxModel)
        comboBox!!.setRenderer(object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(list: JList<*>?, value: Any, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
                val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
                if (value != null) {
                    val configuration = value as RocketActionConfiguration
                    label.text = configuration.type() + " - " + configuration.description()
                    label.toolTipText = configuration.description()
                }
                return label
            }
        })
        comboBox!!.addItemListener { e: ItemEvent ->
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
                            val component = actionUi.create(settings)
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
            buttonTest.addActionListener { e: ActionEvent? -> SwingUtilities.invokeLater { createTest(actionSettingsPanel.create()) } }
            panel.add(buttonTest)
            add(panel, BorderLayout.SOUTH)
        }
    }

    private inner class RocketActionSettingsPanel : JPanel(BorderLayout()) {
        private val tableModel = DefaultTableModel()
        private val table = JTable(tableModel)
        private var currentConfiguration: RocketActionConfiguration? = null
        fun setRocketActionConfiguration(configuration: RocketActionConfiguration) {
            currentConfiguration = configuration
            while (tableModel.rowCount != 0) {
                tableModel.removeRow(0)
            }
            configuration.properties().forEach(Consumer { c: RocketActionConfigurationProperty? ->
                val row = Vector<String?>()
                row.add(c!!.name())
                row.add("")
                row.add(c.description())
                row.add(java.lang.Boolean.toString(c.isRequired))
                tableModel.addRow(row)
            })
        }

        fun create(): RocketActionSettings {
            checkNotNull(currentConfiguration) { "Must be set current selected configuration" }
            val rowCount = tableModel.rowCount
            val map: MutableMap<String, String> = TreeMap()
            for (i in 0 until rowCount) {
                val name = tableModel.getValueAt(i, 0)
                val value = tableModel.getValueAt(i, 1)
                map[name.toString()] = value.toString()
            }
            return NewRocketActionSettings(currentConfiguration!!.type(), map, emptyList())
        }

        init {
            tableModel.addColumn("Name")
            tableModel.addColumn("Value")
            tableModel.addColumn("Description")
            tableModel.addColumn("Required")
            add(JScrollPane(table))
        }
    }

    init {
        dialog = JDialog(owner, "Create rocket action")
        val ownerSize = owner.size
        dialog.setSize((ownerSize.width * 0.7).toInt(), (ownerSize.height * 0.7).toInt())
        this.rocketActionConfigurationRepository = rocketActionConfigurationRepository
        rocketActionConfigurationRepository.load()
        this.rocketActionUiRepository = rocketActionUiRepository
        dialog.add(panelComboBox(), BorderLayout.NORTH)
        actionSettingsPanel.setRocketActionConfiguration(comboBox!!.selectedItem as RocketActionConfiguration)
        dialog.add(actionSettingsPanel, BorderLayout.CENTER)
        dialog.add(createTestAndSaveDialog(), BorderLayout.SOUTH)
        dialog.defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        dialog.setLocationRelativeTo(owner)
    }
}