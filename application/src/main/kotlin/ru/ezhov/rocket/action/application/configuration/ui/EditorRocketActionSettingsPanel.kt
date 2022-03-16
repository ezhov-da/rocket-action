package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.api.PropertyType
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiListener
import ru.ezhov.rocket.action.application.configuration.ui.event.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.configuration.ui.event.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.configuration.ui.event.model.RemoveSettingUiEvent
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.SwingConstants

class EditorRocketActionSettingsPanel(
    private val rocketActionPluginRepository: RocketActionPluginRepository
) : JPanel(BorderLayout()) {
    private val rocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentSettings: TreeRocketActionSettings? = null
    private var callback: SavedRocketActionSettingsPanelCallback? = null
    private val labelType = JLabel()
    private val labelDescription = JLabel()
    private val testPanel: TestPanel =
        TestPanel(rocketActionPluginRepository = rocketActionPluginRepository) {
            rocketActionSettingsPanel.create()?.settings
        }

    private fun top(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(labelType, BorderLayout.NORTH)
        panel.add(labelDescription, BorderLayout.CENTER)
        return panel
    }

    private fun testAndCreate(): JPanel {
        val panel = JPanel()
        val button = JButton("Сохранить конфигурацию текущего действия")
        button.addActionListener {
            rocketActionSettingsPanel.create()?.let { rs ->
                callback!!.saved(rs)
                NotificationFactory.notification.show(NotificationType.INFO, "Конфигурация текущего действия сохранена")
            } ?: run {
                NotificationFactory.notification.show(NotificationType.WARN, "Действие не выбрано")
            }

        }
        panel.add(button)
        return panel
    }

    fun show(settings: TreeRocketActionSettings, callback: SavedRocketActionSettingsPanelCallback) {
        if (stubPanel != null) {
            this.remove(stubPanel)
            stubPanel = null
            this.add(basicPanel)
            this.revalidate()
            this.repaint()
        }
        testPanel.clearTest()
        currentSettings = settings
        labelType.text = settings.settings.type().value()
        this.callback = callback
        val configuration: RocketActionConfiguration? = rocketActionPluginRepository.by(settings.settings.type())?.configuration()
        configuration?.let {
            labelDescription.text = configuration.description()
        }
        val settingsFinal = settings.settings.settings()
        val values = settingsFinal
            .map { (k: RocketActionConfigurationPropertyKey, v: String) ->
                val property = configuration
                    ?.let { conf ->
                        conf
                            .properties()
                            .firstOrNull { p: RocketActionConfigurationProperty? -> p!!.key() == k }
                    }
                Value(
                    key = k,
                    value = v,
                    property = property,
                )
            }
            .toMutableList() +
            configuration
                ?.properties()
                ?.filter { p: RocketActionConfigurationProperty -> !settingsFinal.containsKey(p.key()) }
                ?.map { p: RocketActionConfigurationProperty? ->
                    Value(
                        key = p!!.key(),
                        value = "",
                        property = p,
                    )
                }.orEmpty()


        rocketActionSettingsPanel
            .setRocketActionConfiguration(
                values
                    .sortedWith(
                        compareByDescending<Value> { it.property?.isRequired() }
                            .thenBy { it.property?.name() }
                    )
            )
    }

    private data class Value(
        val key: RocketActionConfigurationPropertyKey,
        val value: String,
        val property: RocketActionConfigurationProperty?
    )

    private inner class RocketActionSettingsPanel : JPanel() {
        private val settingPanels = mutableListOf<SettingPanel>()
        private var values: List<Value>? = null

        init {
            this.layout = BoxLayout(this, BoxLayout.Y_AXIS)
        }

        fun setRocketActionConfiguration(list: List<Value>) {
            removeAll()
            settingPanels.clear()
            this.values = list
            list
                .forEach { v: Value ->
                    val panel = SettingPanel(v)
                    this.add(panel)
                    settingPanels.add(panel)
                }
            this.add(Box.createVerticalBox())
            repaint()
            revalidate()
        }

        fun create(): TreeRocketActionSettings? = currentSettings?.let { rs ->
            TreeRocketActionSettings(
                configuration = rs.configuration,
                settings = MutableRocketActionSettings(
                    rs.settings.id(),
                    rs.settings.type(),
                    settingPanels.associate { panel -> panel.value() }.toMutableMap(),
                    rs.settings.actions().toMutableList()
                )
            )
        }
    }

    private class SettingPanel(private val value: Value) : JPanel() {
        private var valueCallback: () -> String = { "" }

        init {
            this.layout = BorderLayout()
            this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
            value.property?.let { property ->
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
                                    tp.text = value.value
                                }
                            ),
                            BorderLayout.CENTER
                        )
                    }
                    PropertyType.BOOLEAN -> {
                        centerPanel.add(JScrollPane(
                            JCheckBox().also { cb ->
                                cb.isSelected = value.value.toBoolean()
                                valueCallback = { cb.isSelected.toString() }
                            }
                        ), BorderLayout.CENTER)
                    }
                }

                this.add(topPanel, BorderLayout.NORTH)
                this.add(centerPanel, BorderLayout.CENTER)
            } ?: run {
                this.add(JLabel("Обнаружено незарегистрированное свойство '${value.key}:${value.value}'"), BorderLayout.CENTER)
                valueCallback = { value.value }
            }
        }

        fun value(): Pair<RocketActionConfigurationPropertyKey, String> = Pair(first = value.key, valueCallback())
    }

    private var stubPanel: JPanel? = null
    private var basicPanel: JPanel = JPanel(BorderLayout())

    init {
        basicPanel.add(top(), BorderLayout.NORTH)
        basicPanel.add(JScrollPane(rocketActionSettingsPanel), BorderLayout.CENTER)

        val southPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(testPanel)
            add(testAndCreate())
        }
        basicPanel.add(southPanel, BorderLayout.SOUTH)

        add(basicPanel, BorderLayout.CENTER)

        val editorPanel = this
        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is RemoveSettingUiEvent && event.countChildrenRoot == 0) {
                    stubPanel = JPanel(BorderLayout()).apply {
                        add(
                            JLabel("Создайте первое действие").apply {
                                horizontalTextPosition = SwingConstants.CENTER
                                horizontalAlignment = SwingConstants.CENTER
                                verticalTextPosition = SwingConstants.CENTER
                                verticalAlignment = SwingConstants.CENTER
                            },
                            BorderLayout.CENTER
                        )
                    }
                    editorPanel.remove(basicPanel)
                    editorPanel.add(stubPanel, BorderLayout.CENTER)
                    editorPanel.revalidate()
                    editorPanel.repaint()
                }
            }
        })
    }
}