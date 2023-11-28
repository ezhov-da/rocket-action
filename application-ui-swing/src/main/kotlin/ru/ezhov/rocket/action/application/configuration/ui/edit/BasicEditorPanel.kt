package ru.ezhov.rocket.action.application.configuration.ui.edit

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.configuration.ui.SavedRocketActionSettingsPanelCallback
import ru.ezhov.rocket.action.application.configuration.ui.TestPanel
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel

class BasicEditorPanel(
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    engineService: EngineService,
    availableHandlersRepository: AvailableHandlersRepository,
    tagsService: TagsService,
) : JPanel(BorderLayout()) {
    private val infoPanel: InfoPanel =
        InfoPanel(availableHandlersRepository)
    private val rocketActionSettingsPanel = RocketActionSettingsPanel(
        tagsService = tagsService,
        rocketActionContextFactory = rocketActionContextFactory
    )
    private val testPanel: TestPanel =
        TestPanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionContextFactory = rocketActionContextFactory,
            engineService = engineService,
        ) {
            rocketActionSettingsPanel.create()?.settings
        }

    private var callback: SavedRocketActionSettingsPanelCallback? = null

    init {
        add(infoPanel, BorderLayout.NORTH)
        add(rocketActionSettingsPanel, BorderLayout.CENTER)

        val southPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(testPanel)
            add(testAndCreate())
        }
        add(southPanel, BorderLayout.SOUTH)
    }

    private fun testAndCreate(): JPanel {
        val panel = JPanel()
        val button = JButton(
            "Save current action configuration to tree",
            rocketActionContextFactory.context.icon().by(AppIcon.SAVE)
        )
        button.addActionListener {
            rocketActionSettingsPanel.create()?.let { rs ->
                callback!!.saved(rs)
                rocketActionContextFactory.context.notification()
                    .show(NotificationType.INFO, "Current action configuration saved")
            } ?: run {
                rocketActionContextFactory.context.notification().show(NotificationType.WARN, "Action not selected")
            }
        }
        panel.add(button)
        return panel
    }

    fun fillPanel(settings: TreeRocketActionSettings, callback: SavedRocketActionSettingsPanelCallback) {
        this.callback = callback
        testPanel.clearTest()
        val configuration: RocketActionConfiguration? =
            rocketActionPluginApplicationService.by(settings.settings.type)
                ?.configuration(rocketActionContextFactory.context)
        infoPanel.refresh(
            type = settings.settings.type,
            rocketActionId = settings.settings.id,
        )
        val settingsFinal = settings.settings.settings
        val values = settingsFinal
            .map { settingsModel ->
                val property = configuration
                    ?.let { conf ->
                        conf
                            .properties()
                            .firstOrNull { p: RocketActionConfigurationProperty? ->
                                p!!.key() == settingsModel.name
                            }
                    }
                Value(
                    key = settingsModel.name,
                    value = settingsModel.value,
                    property = property,
                    valueType = settingsModel.valueType,
                )
            }
            .toMutableList() +
            configuration // looking for the properties that have been added
                ?.properties()
                ?.filter { p: RocketActionConfigurationProperty ->
                    settingsFinal.firstOrNull { it.name == p.key() } == null
                }
                ?.map { p: RocketActionConfigurationProperty? ->
                    Value(
                        key = p!!.key(),
                        value = "",
                        property = p,
                        valueType = null,
                    )
                }.orEmpty()

        rocketActionSettingsPanel
            .setRocketActionConfiguration(
                settings = settings,
                rocketActionType = settings.configuration.type().value(),
                list = values
                    .sortedWith(
                        compareByDescending<Value> { it.property?.isRequired() }
                            .thenBy { it.property?.name() }
                    ),
                tags = settings.settings.tags,
            )
    }
}
