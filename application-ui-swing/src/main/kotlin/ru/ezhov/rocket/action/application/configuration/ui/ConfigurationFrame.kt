package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.configuration.ui.create.CreateRocketActionSettingsDialog
import ru.ezhov.rocket.action.application.configuration.ui.edit.EditorRocketActionSettingsPanel
import ru.ezhov.rocket.action.application.configuration.ui.tree.ConfigurationTreePanel
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.event.ActionModelSavedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.ui.PluginManagerFrame
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import ru.ezhov.rocket.action.application.variables.interfaces.ui.VariablesFrame
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class ConfigurationFrame(
    rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsService: RocketActionSettingsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val engineService: EngineService,
    private val availableHandlersRepository: AvailableHandlersRepository,
    private val tagsService: TagsService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    private val variablesApplication: VariablesApplication,
    updateActionListener: ActionListener,
) {
    private val frame: JFrame = JFrame()
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog
    private val updateActionListener: ActionListener


    private fun panel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(commonPanel(), BorderLayout.CENTER)
        return panel
    }

    private fun commonPanel(): JPanel {
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT)
        SwingUtilities.invokeLater {
            splitPane.setDividerLocation(0.3)
            splitPane.resizeWeight = 0.3
        }

        setTitle(rocketActionSettingsService.actionsModel().lastChangedDate)

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                if (event is ActionModelSavedDomainEvent) {
                    setTitle(event.actionsModel.lastChangedDate)
                }
            }

            override fun subscribedToEventType(): List<Class<*>> = listOf(ActionModelSavedDomainEvent::class.java)

        })

        val editorRocketActionSettingsPanel = EditorRocketActionSettingsPanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionContextFactory = rocketActionContextFactory,
            engineService = engineService,
            availableHandlersRepository = availableHandlersRepository,
            tagsService = tagsService,
        )
        val panel = JPanel(BorderLayout())

        splitPane.leftComponent = ConfigurationTreePanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionSettingsService = rocketActionSettingsService,
            rocketActionContextFactory = rocketActionContextFactory,
            createRocketActionSettingsDialog = createRocketActionSettingsDialog,
            editorRocketActionSettingsPanel = editorRocketActionSettingsPanel,
        )
        splitPane.rightComponent = editorRocketActionSettingsPanel


        panel.add(splitPane, BorderLayout.CENTER)
        return panel
    }

    private fun setTitle(dateTime: LocalDateTime) {
        val dateAsString = dateTime.format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        frame.title = "Configuring actions '$dateAsString'"
    }

    fun show() {
        frame.showToFront()
    }

    private var menuBar: JToolBar?

    init {
        frame.iconImage = rocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage()
        frame.isAlwaysOnTop = generalPropertiesRepository
            .asBoolean(UsedPropertiesName.UI_CONFIGURATION_FRAME_ALWAYS_ON_TOP, false)
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE
        this.updateActionListener = updateActionListener
        this.rocketActionPluginApplicationService = rocketActionPluginApplicationService

        val size = Toolkit.getDefaultToolkit().screenSize
        frame.setSize(
            (size.width * generalPropertiesRepository
                .asFloat(
                    UsedPropertiesName.UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT,
                    0.6F
                )
                ).toInt(),
            (size.height * generalPropertiesRepository
                .asFloat(
                    UsedPropertiesName.UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT,
                    0.6F
                )
                ).toInt()
        )
        frame.setLocationRelativeTo(null)
        createRocketActionSettingsDialog = CreateRocketActionSettingsDialog(
            owner = frame,
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionContextFactory = rocketActionContextFactory,
            engineService = engineService,
            tagsService = tagsService,
        )

        val basePanel = JPanel(BorderLayout())

        menuBar = createToolBar()

        basePanel.add(menuBar, BorderLayout.NORTH)
        basePanel.add(panel(), BorderLayout.CENTER)

        frame.add(basePanel, BorderLayout.CENTER)
    }

    private fun createToolBar(): JToolBar {
        val menuBar = JToolBar()

        // Refresh
        menuBar.add(
            JButton("Refresh").apply {
                icon = rocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
                addActionListener { e: ActionEvent? ->
                    SwingUtilities.invokeLater {
                        updateActionListener?.actionPerformed(e)
                        ConfigurationFrame@ frame.isVisible = false
                    }
                }
            })

        // Variables
        menuBar.add(JButton("Variables").apply {
            val variablesFrame = VariablesFrame(
                parent = frame,
                variablesApplication = variablesApplication,
                notificationService = rocketActionContextFactory.context.notification(),
                iconService = rocketActionContextFactory.context.icon()
            )
            icon = rocketActionContextFactory.context.icon().by(AppIcon.FORK)
            addActionListener {
                SwingUtilities.invokeLater {
                    variablesFrame.isVisible = true
                }
            }
        })

        // Plugin Download Information
        menuBar.add(JButton("Plugin Download Information").apply {
            val pluginManagerFrame = PluginManagerFrame(
                rocketActionPluginApplicationService = rocketActionPluginApplicationService,
                parent = frame
            )
            icon = rocketActionContextFactory.context.icon().by(AppIcon.INFO)
            addActionListener {
                SwingUtilities.invokeLater {
                    pluginManagerFrame.isVisible = true
                }
            }
        })

        return menuBar
    }
}
