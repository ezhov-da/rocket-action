package ru.ezhov.rocket.action.application.configuration.ui.create

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.tags.ui.create.TagsPanel
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class CreateRocketActionSettingsPanel (
    private val tagsPanel: TagsPanel,
    private val rocketActionContextFactory: RocketActionContextFactory,
) : JPanel() {
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
                val panel = SettingPanel(rocketActionContextFactory, p)
                this.add(panel)
                settingPanels.add(panel)
            }
        tagsPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Tags")
        )
        // disable tags for group plugin
        if (configuration.type().value() != GroupRocketActionUi.TYPE) {
            this.add(tagsPanel)
        }
        tagsPanel.clearTags()
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
