package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.application.configuration.ContractGenerator
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.tags.ui.create.TagsPanel
import ru.ezhov.rocket.action.ui.utils.swing.MarkdownEditorPane
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JTabbedPane

class CreateRocketActionSettingsPanel(
    private val tagsPanel: TagsPanel,
) : JPanel() {
    private val settingPanels = mutableListOf<SettingPanel>()
    private var currentConfiguration: RocketActionConfiguration? = null

    init {
        this.layout = BorderLayout()
    }

    fun setRocketActionConfiguration(configuration: RocketActionConfiguration) {
        removeAll()
        settingPanels.clear()

        val tabs = JTabbedPane().apply {
            this.tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT
            this.tabPlacement = JTabbedPane.LEFT
        }

        this.currentConfiguration = configuration
        val properties = configuration
            .properties()
            .sortedWith(
                compareByDescending<RocketActionConfigurationProperty> { it.isRequired() }
                    .thenBy { it.name() }
            )


        properties.forEach { p: RocketActionConfigurationProperty ->
            val panel = SettingPanel(p)
            tabs.addTab(panel.labelText(), panel)
            settingPanels.add(panel)
        }

        val generalTabs = JTabbedPane()
        generalTabs.addTab("Configuration", tabs)
        generalTabs.addTab("Info", MarkdownEditorPane.fromText(configuration.description()))
        generalTabs.addTab(
            "Contract",
            MarkdownEditorPane.fromText(ContractGenerator.generateToMarkDown(properties))
        )

        this.add(generalTabs, BorderLayout.CENTER)

        tagsPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Tags")
        )
        // disable tags for group plugin
        if (configuration.type().value() != GroupRocketActionUi.TYPE) {
            this.add(tagsPanel, BorderLayout.SOUTH)
        }
        tagsPanel.clearTags()
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
