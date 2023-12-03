package ru.ezhov.rocket.action.application.chainaction.interfaces.ui

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.actions.ActionsConfigurationPanel
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.configuration.chains.ChainsConfigurationPanel
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiListener
import ru.ezhov.rocket.action.application.eventui.ConfigurationUiObserverFactory
import ru.ezhov.rocket.action.application.eventui.model.ConfigurationUiEvent
import ru.ezhov.rocket.action.application.eventui.model.ShowChainActionConfigurationUiEvent
import ru.ezhov.rocket.action.ui.utils.swing.common.SizeUtil
import ru.ezhov.rocket.action.ui.utils.swing.common.showToFront
import ru.ezhov.rocket.action.ui.utils.swing.common.toImage
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.KeyStroke

class ChainConfigurationFrame(
    actionExecutorService: ActionExecutorService,
    chainActionService: ChainActionService,
    atomicActionService: AtomicActionService,
) : JFrame() {
    private val contentPane = JPanel(MigLayout(/*"debug"*/))

    private val createChainActionDialog = CreateAndEditChainActionDialog(
        actionExecutorService = actionExecutorService,
        chainActionService = chainActionService,
        atomicActionService = atomicActionService,
    )

    private val actionsConfigurationPanel = ActionsConfigurationPanel(
        atomicActionService = atomicActionService,
        chainActionService = chainActionService,
        createAndEditChainActionDialog = createChainActionDialog
    )

    private val chainsConfigurationPanel = ChainsConfigurationPanel(
        actionExecutorService = actionExecutorService,
        atomicActionService = atomicActionService,
        chainActionService = chainActionService,
        createAndEditChainActionDialog = createChainActionDialog,
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

        contentPane.add(panelChainAction, "width 50%")
        contentPane.add(panelAtomicAction, "width 50%")

        setContentPane(contentPane)

        // call onCancel() when cross is clicked
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                onCancel()
            }
        })

        // call onCancel() on ESCAPE
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

    private fun onCancel() {
        // add your code here if necessary
        dispose()
    }
}
