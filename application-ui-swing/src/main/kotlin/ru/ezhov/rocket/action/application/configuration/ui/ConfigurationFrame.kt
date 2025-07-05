package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.about.AboutDialogFactory
import ru.ezhov.rocket.action.application.availableproperties.AvailablePropertiesFromCommandLineDialogFactory
import ru.ezhov.rocket.action.application.configuration.ui.create.CreateRocketActionSettingsDialog
import ru.ezhov.rocket.action.application.configuration.ui.edit.EditorRocketActionSettingsPanel
import ru.ezhov.rocket.action.application.configuration.ui.tree.ConfigurationTreePanel
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.event.ActionModelSavedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.RefreshUiEvent
import ru.ezhov.rocket.action.application.handlers.apikey.application.ApiKeysApplication
import ru.ezhov.rocket.action.application.handlers.apikey.interfaces.ui.ApiKeysFrame
import ru.ezhov.rocket.action.application.handlers.server.AvailableHandlersRepository
import ru.ezhov.rocket.action.application.handlers.server.HttpServerService
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.ui.PluginManagerFrame
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.application.search.application.SearchTextTransformer
import ru.ezhov.rocket.action.application.tags.application.TagsService
import ru.ezhov.rocket.action.application.variables.application.VariablesApplication
import ru.ezhov.rocket.action.application.variables.interfaces.ui.VariablesFrame
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class ConfigurationFrame(
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsService: RocketActionSettingsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val engineService: EngineService,
    private val availableHandlersRepository: AvailableHandlersRepository,
    private val tagsService: TagsService,
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val variablesApplication: VariablesApplication,
    private val aboutDialogFactory: AboutDialogFactory,
    private val httpServerService: HttpServerService,
    private val availablePropertiesFromCommandLineDialogFactory: AvailablePropertiesFromCommandLineDialogFactory,
    private val apiKeysApplication: ApiKeysApplication,
    private val searchTextTransformer: SearchTextTransformer,
) {
    val frame: JFrame = JFrame()
    private val createRocketActionSettingsDialog: CreateRocketActionSettingsDialog

    init {
        frame.iconImage = rocketActionContextFactory.context.icon().by(AppIcon.ROCKET_APP).toImage()
        frame.isAlwaysOnTop = generalPropertiesRepository
            .asBoolean(UsedPropertiesName.UI_CONFIGURATION_FRAME_ALWAYS_ON_TOP, false)
        frame.defaultCloseOperation = JFrame.HIDE_ON_CLOSE

        frame.size =
            SizeUtil.dimension(
                generalPropertiesRepository
                    .asDouble(
                        UsedPropertiesName.UI_CONFIGURATION_DIALOG_WIDTH_IN_PERCENT,
                        0.6
                    ),
                generalPropertiesRepository
                    .asDouble(
                        UsedPropertiesName.UI_CONFIGURATION_DIALOG_HEIGHT_IN_PERCENT,
                        0.6
                    )
            )

        frame.setLocationRelativeTo(null)
        createRocketActionSettingsDialog = CreateRocketActionSettingsDialog(
            owner = frame,
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionContextFactory = rocketActionContextFactory,
            engineService = engineService,
            tagsService = tagsService,
            generalPropertiesRepository = generalPropertiesRepository,
        )

        val basePanel = JPanel(BorderLayout())

        val toolBar = createToolBar()

        basePanel.add(toolBar, BorderLayout.PAGE_START)
        basePanel.add(panel(), BorderLayout.CENTER)

        frame.add(basePanel, BorderLayout.CENTER)
    }

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
            generalPropertiesRepository = generalPropertiesRepository,
        )
        val panel = JPanel(BorderLayout())

        splitPane.leftComponent = ConfigurationTreePanel(
            rocketActionPluginApplicationService = rocketActionPluginApplicationService,
            rocketActionSettingsService = rocketActionSettingsService,
            rocketActionContextFactory = rocketActionContextFactory,
            createRocketActionSettingsDialog = createRocketActionSettingsDialog,
            editorRocketActionSettingsPanel = editorRocketActionSettingsPanel,
            searchTextTransformer = searchTextTransformer,
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

    private var variablesFrame: VariablesFrame? = null
    private var apiKeysFrame: ApiKeysFrame? = null

    fun getVariablesFrame(): VariablesFrame = variablesFrame!!

    private fun createToolBar(): JToolBar {
        val toolBar = JToolBar()

        toolBar.add(
            JButton().apply {
                toolTipText = "Reload actions and close config window"
                icon = rocketActionContextFactory.context.icon().by(AppIcon.RELOAD)
                addActionListener { e: ActionEvent? ->
                    SwingUtilities.invokeLater {
                        ConfigurationUiObserverFactory.observer.notify(RefreshUiEvent())
                        ConfigurationFrame@ frame.isVisible = false
                    }
                }
            })

        toolBar.add(JButton().apply {
            toolTipText = "Variables"
            variablesFrame = VariablesFrame(
                parent = frame,
                variablesApplication = variablesApplication,
                notificationService = rocketActionContextFactory.context.notification(),
                iconService = rocketActionContextFactory.context.icon()
            )
            icon = rocketActionContextFactory.context.icon().by(AppIcon.FORK)
            addActionListener {
                SwingUtilities.invokeLater {
                    variablesFrame!!.isVisible = true
                }
            }
        })

        toolBar.addSeparator()

        toolBar.add(
            JButton().apply {
                toolTipText = "API handlers"
                icon = rocketActionContextFactory.context.icon().by(AppIcon.LINK_INTACT)
                addActionListener {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(URI.create(httpServerService.serverUrl()))
                    }
                }
            }
        )

        toolBar.add(
            JButton().apply {
                toolTipText = "API keys"
                icon = rocketActionContextFactory.context.icon().by(AppIcon.SHIELD)
                addActionListener {
                    apiKeysFrame = ApiKeysFrame(
                        parent = frame,
                        apiKeysApplication = apiKeysApplication,
                        notificationService = rocketActionContextFactory.context.notification(),
                        iconService = rocketActionContextFactory.context.icon()
                    )
                    SwingUtilities.invokeLater {
                        apiKeysFrame!!.isVisible = true
                    }
                }
            }
        )

        toolBar.addSeparator()

        toolBar.add(JButton().apply {
            toolTipText = "Plugins download information"
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

        toolBar.add(JButton().apply
        {
            toolTipText = "Properties from the command line"
            icon = Icons.Standard.LIST_16x16
            addActionListener {
                SwingUtilities.invokeLater {
                    availablePropertiesFromCommandLineDialogFactory.properties.showDialog()
                }
            }
        })

        toolBar.add(JButton().apply
        {
            toolTipText = "About"
            icon = Icons.Standard.QUESTION_MARK_16x16
            addActionListener {
                SwingUtilities.invokeLater {
                    aboutDialogFactory.about.showAboutDialog()
                }
            }
        })

        toolBar.add(Box.createHorizontalGlue())

        return toolBar
    }
}
