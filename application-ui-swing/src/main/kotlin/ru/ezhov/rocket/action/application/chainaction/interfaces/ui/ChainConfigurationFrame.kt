package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.ActionsConfigurationPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains.ChainsConfigurationPanel
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.ActionSchedulerService
import ru.ezhov.rocket.action.application.chainaction.scheduler.interfaces.ui.AllActionSchedulersFrame
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionRunner
import ru.ezhov.rocket.action.application.chainaction.startup.application.StartupActionService
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.chainaction.startup.interfaces.ui.StartupActionsFrame
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiListener
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.eventui.model.ShowChainActionConfigurationUiEvent
import ru.ezhov.rocket.action.application.icon.infrastructure.IconRepository
import ru.ezhov.rocket.action.application.resources.Icons
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

class ChainConfigurationFrame(
    actionExecutorService: ActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
    actionExecutor: ActionExecutor,
    iconRepository: IconRepository,
    private val actionSchedulerService: ActionSchedulerService,
    private val startupActionService: StartupActionService,
    private val startupActionRunner: StartupActionRunner,
    private val startupActionLogRepository: StartupActionLogRepository,
) : JFrame() {
    private val contentPane = JPanel(MigLayout(/*"debug"*/))

    private val openSchedulersButton = JButton("Schedulers", Icons.Standard.CLOCK_16x16).apply {
        toolTipText = "Open schedulers"
    }

    private val openStartupButton = JButton("Startup", Icons.Standard.POWER_STANDBY_16x16).apply {
        toolTipText = "Open the actions executed when the application starts"
    }

    private var actionSchedulersFrame: AllActionSchedulersFrame? = null
    private var startupActionsFrame: StartupActionsFrame? = null

    private val createChainActionDialog = CreateAndEditChainActionDialog(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
        iconRepository = iconRepository,
        actionSchedulerService = actionSchedulerService,
    )

    private val actionsConfigurationPanel = ActionsConfigurationPanel(
        atomicActionService = atomicActionService,
        chainActionService = chainActionService,
        createAndEditChainActionDialog = createChainActionDialog,
        actionExecutorService = actionExecutorService,
        iconRepository = iconRepository,
        actionSchedulerService = actionSchedulerService,
    )

    private val chainsConfigurationPanel = ChainsConfigurationPanel(
        actionExecutorService = actionExecutorService,
        atomicActionService = atomicActionService,
        chainActionService = chainActionService,
        createAndEditChainActionDialog = createChainActionDialog,
        actionSchedulerService = actionSchedulerService
    )

    init {
        val chainConfigurationFrame = this
        ConfigurationUiObserverFactory.observer.register(object : ConfigurationUiListener {
            override fun action(event: ConfigurationUiEvent) {
                if (event is ShowChainActionConfigurationUiEvent) {
                    chainConfigurationFrame.showToFront(event.parent)
                }
            }
        })

        val panelAtomicAction = JPanel(MigLayout())
        panelAtomicAction.add(actionsConfigurationPanel, "height max, width max")

        val panelChainAction = JPanel(MigLayout())
        panelChainAction.add(chainsConfigurationPanel, "height max, width max")

        val panelToolbar = JPanel(MigLayout("insets 5 5 0 5"))
        panelToolbar.add(openSchedulersButton, "align left")
        panelToolbar.add(openStartupButton, "align left")

        contentPane.add(panelToolbar, "span 2, width 100%, wrap")
        contentPane.add(panelChainAction, "width 50%")
        contentPane.add(panelAtomicAction, "width 50%")

        openSchedulersButton.addActionListener {
            openSchedulers()
        }

        openStartupButton.addActionListener {
            openStartupActions()
        }

        setContentPane(contentPane)

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() or ESCAPE
        contentPane.registerKeyboardAction(
            { onCancel() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        iconImage = CHAIN_ICON.toImage()
        title = "Chains"

        size = SizeUtil.dimension(0.8, 0.8)
        setLocationRelativeTo(null)
    }

    private fun onOK() {
        // add your code here
        dispose()
    }

    /**
     * Opens the window with all action schedulers.
     */
    fun openSchedulers() {
        SwingUtilities.invokeLater {
            val frame = actionSchedulersFrame
                ?: AllActionSchedulersFrame(actionSchedulerService)
                    .also { actionSchedulersFrame = it }

            frame.showToFront(this)
        }
    }

    /**
     * Opens the window with the actions executed when the application starts.
     */
    fun openStartupActions() {
        SwingUtilities.invokeLater {
            val frame = startupActionsFrame
                ?: StartupActionsFrame(
                    startupActionService = startupActionService,
                    startupActionRunner = startupActionRunner,
                    startupActionLogRepository = startupActionLogRepository,
                ).also { startupActionsFrame = it }

            frame.showToFront(this)
        }
    }

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
