package ru.ezhov.rocket.action.application.configuration.ui.edit

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.tags.ui.create.TagsPanelFactory
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

class RocketActionSettingsPanel(
    tagsService: TagsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
) : JPanel() {
    private var currentSettings: TreeRocketActionSettings? = null
    private val settingPanels = mutableListOf<SettingPanel>()
    private var values: List<Value>? = null
    private val tagsPanel = TagsPanelFactory.panel(tagsService = tagsService)

    init {
        this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }

    fun setRocketActionConfiguration(
        settings: TreeRocketActionSettings,
        rocketActionType: String,
        list: List<Value>,
        tags: List<String>
    ) {
        this.currentSettings = settings
        removeAll()
        settingPanels.clear()
        this.values = list
        list
            .forEach { v: Value ->
                val panel = SettingPanel(rocketActionContextFactory, v)
                this.add(panel)
                settingPanels.add(panel)
            }
        tagsPanel.setTags(tags)
        tagsPanel.border = BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createTitledBorder("Tags")
        )
        // disable tags for group plugin
        if (rocketActionType != GroupRocketActionUi.TYPE) {
            this.add(tagsPanel)
        }
        this.add(Box.createVerticalBox())
        repaint()
        revalidate()
    }

    fun create(): TreeRocketActionSettings? = currentSettings?.let { rs ->
        TreeRocketActionSettings(
            configuration = rs.configuration,
            settings = MutableRocketActionSettings(
                id = rs.settings.id,
                type = rs.settings.type,
                settings = settingPanels.map { panel -> panel.value() }.toMutableList(),
                actions = rs.settings.actions.toMutableList(),
                tags = tagsPanel.tags(),
            )
        )
    }
}
