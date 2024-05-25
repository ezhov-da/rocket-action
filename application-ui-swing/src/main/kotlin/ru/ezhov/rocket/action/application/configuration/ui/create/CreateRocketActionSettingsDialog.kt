package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.configuration.ui.TestPanel
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.tags.ui.create.TagsPanelFactory
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ItemEvent
import java.util.function.Consumer
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

class CreateRocketActionSettingsDialog(
    owner: JFrame,
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    engineService: EngineService,
    tagsService: TagsService,
) {
    private val comboBoxModel = DefaultComboBoxModel<RocketActionConfiguration>()
    private var comboBox: JComboBox<RocketActionConfiguration> = JComboBox(comboBoxModel)
    private val actionSettingsPanel: CreateRocketActionSettingsPanel =
        CreateRocketActionSettingsPanel(
            tagsPanel = TagsPanelFactory.panel(tagsService = tagsService),
        )
    private var currentCallback: CreatedRocketActionSettingsCallback? = null
    private val testPanel: TestPanel =
        TestPanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionContextFactory = rocketActionContextFactory,
            engineService = engineService,
        ) {
            actionSettingsPanel.create().second
        }

    private val dialog: JDialog = JDialog(owner, "Create action").apply {
        this.setIconImage(rocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage())
        val ownerSize = owner.size
        setSize((ownerSize.width * 0.7).toInt(), (ownerSize.height * 0.7).toInt())

        add(panelComboBox(), BorderLayout.NORTH)

        val ra = comboBox.selectedItem as RocketActionConfiguration
        actionSettingsPanel.setRocketActionConfiguration(ra)

        add(actionSettingsPanel, BorderLayout.CENTER)

        add(createTestAndSaveDialog(), BorderLayout.SOUTH)
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        setLocationRelativeTo(owner)
    }

    private fun panelComboBox(): JPanel {
        val all = rocketActionPluginApplicationService
            .all()
            .map { it.configuration(rocketActionContextFactory.context) }
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
                    configuration.icon()?.let { icon ->
                        label.icon = icon
                    }
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
        val buttonCreate = JButton("Create action")
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
}
