package ru.ezhov.rocket.action.application.configuration.ui.edit

import ru.ezhov.rocket.action.application.configuration.ui.SavedRocketActionSettingsPanelCallback
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiListener
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.configuration.ui.event.model.RemoveSettingUiEvent
import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants

class EditorRocketActionSettingsPanel(
    rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    rocketActionContextFactory: RocketActionContextFactory,
    engineService: EngineService,
    availableHandlersRepository: AvailableHandlersRepository,
    tagsService: TagsService,
) : JPanel(BorderLayout()) {
    private var currentSettings: TreeRocketActionSettings? = null

    private var stubPanel: JPanel = createStubPanel("Create or select an existing configuration")
    private var basicEditorPanel = BasicEditorPanel(
        rocketActionPluginApplicationService = rocketActionPluginApplicationService,
        rocketActionContextFactory = rocketActionContextFactory,
        engineService = engineService,
        availableHandlersRepository = availableHandlersRepository,
        tagsService = tagsService,
    )
    private var currentPanel: JPanel = stubPanel

    fun show(settings: TreeRocketActionSettings, callback: SavedRocketActionSettingsPanelCallback) {
        currentSettings = settings
        basicEditorPanel.fillPanel(settings, callback)
        setCurrentPanel(basicEditorPanel)
    }

    init {
        setCurrentPanel(stubPanel)

        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is RemoveSettingUiEvent) {
                    fun recursiveSearchInSettings(
                        settings: MutableRocketActionSettings,
                        id: String,
                        result: MutableList<MutableRocketActionSettings>
                    ) {
                        if (settings.id == id) {
                            result.add(settings)
                        } else {
                            settings.actions.forEach { sett ->
                                recursiveSearchInSettings(sett, id, result)
                            }
                        }
                    }

                    val result = mutableListOf<MutableRocketActionSettings>()
                    recursiveSearchInSettings(
                        event.treeRocketActionSettings.settings,
                        currentSettings!!.settings.id,
                        result
                    )

                    if (event.countChildrenRoot == 0 || result.isNotEmpty()
                    ) {
                        setCurrentPanel(stubPanel)
                    }
                }
            }
        })
    }

    private fun setCurrentPanel(newPanel: JPanel) {
        remove(currentPanel)
        currentPanel = newPanel
        add(newPanel, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    private fun createStubPanel(text: String): JPanel = JPanel(BorderLayout()).apply {
        add(
            JLabel(text).apply {
                horizontalTextPosition = SwingConstants.CENTER
                horizontalAlignment = SwingConstants.CENTER
                verticalTextPosition = SwingConstants.CENTER
                verticalAlignment = SwingConstants.CENTER
            },
            BorderLayout.CENTER
        )
    }
}
