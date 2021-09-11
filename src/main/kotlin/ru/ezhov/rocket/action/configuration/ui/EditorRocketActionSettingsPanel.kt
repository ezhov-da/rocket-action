package ru.ezhov.rocket.action.configuration.ui

import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.configuration.domain.RocketActionConfigurationRepository
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.MutableRocketActionSettings
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextPane

class EditorRocketActionSettingsPanel(
        private val rocketActionConfigurationRepository: RocketActionConfigurationRepository,
        rocketActionUiRepository: RocketActionUiRepository
) : JPanel(BorderLayout()) {
    private val rocketActionSettingsPanel = RocketActionSettingsPanel()
    private var currentSettings: RocketActionSettings? = null
    private var callback: SavedRocketActionSettingsPanelCallback? = null
    private val labelType = JLabel()
    private val labelDescription = JLabel()
    private val testPanel: TestPanel = TestPanel(rocketActionUiRepository = rocketActionUiRepository) { rocketActionSettingsPanel.create() }
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

    fun show(settings: RocketActionSettings, callback: SavedRocketActionSettingsPanelCallback) {
        testPanel.clearTest()
        currentSettings = settings
        labelType.text = settings.type().value()
        this.callback = callback
        val configuration: RocketActionConfiguration? = rocketActionConfigurationRepository.by(settings.type())
        configuration?.let {
            labelDescription.text = configuration.description()
        }
        val settingsFinal = settings.settings()
        val values = settingsFinal.map { (k: String, v: String) ->
            val (description, required, name) = configuration
                    ?.let { conf ->
                        conf
                                .properties()
                                .firstOrNull { p: RocketActionConfigurationProperty? -> p!!.key() == k }
                                ?.let { Triple(first = it.description(), second = it.isRequired, third = it.name()) }
                    } ?: Triple(first = "", second = false, third = "")
            Value(
                    key = k,
                    name = name,
                    value = v,
                    description = description,
                    required = required
            )
        }
                .toMutableList() +
                configuration
                        ?.properties()
                        ?.filter { p: RocketActionConfigurationProperty -> !settingsFinal.containsKey(p.key()) }
                        ?.map { p: RocketActionConfigurationProperty? ->
                            Value(
                                    key = p!!.key(),
                                    name = p.name(),
                                    value = "",
                                    description = p.description(),
                                    required = p.isRequired
                            )
                        }.orEmpty()


        rocketActionSettingsPanel
                .setRocketActionConfiguration(
                        values
                                .sortedWith(
                                        compareByDescending<Value> { it.required }
                                                .thenBy { it.name }
                                )
                )
    }

    private data class Value(
            val key: String,
            val name: String,
            val value: String,
            val description: String,
            val required: Boolean
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

        fun create(): RocketActionSettings? = currentSettings?.let { rs ->
            MutableRocketActionSettings(
                    rs.id(),
                    rs.type(),
                    settingPanels.associate { panel -> panel.value() }.toMutableMap(),
                    rs.actions().toMutableList()
            )
        }
    }

    private class SettingPanel(private val value: Value) : JPanel() {
        private val valueTextArea = JTextPane()

        init {
            this.layout = BorderLayout()
            this.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
            val labelName = JLabel(value.name)
            val labelDescription = JLabel(IconRepositoryFactory.repository.by(AppIcon.INFO))
            labelDescription.toolTipText = value.description

            val topPanel = JPanel()
            topPanel.layout = BoxLayout(topPanel, BoxLayout.X_AXIS)
            topPanel.border = BorderFactory.createEmptyBorder(0, 0, 1, 0)
            topPanel.add(labelName)
            if (value.required) {
                topPanel.add(JLabel("*").apply { foreground = Color.RED })
            }
            topPanel.add(labelDescription)
            val centerPanel = JPanel(BorderLayout())
            centerPanel.add(JScrollPane(valueTextArea), BorderLayout.CENTER)
            valueTextArea.text = value.value
            this.add(topPanel, BorderLayout.NORTH)
            this.add(centerPanel, BorderLayout.CENTER)
        }

        fun value(): Pair<String, String> = Pair(first = value.key, valueTextArea.text)
    }

    init {
        add(top(), BorderLayout.NORTH)
        add(JScrollPane(rocketActionSettingsPanel), BorderLayout.CENTER)

        val southPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(testPanel)
            add(testAndCreate())
        }
        add(southPanel, BorderLayout.SOUTH)
    }
}