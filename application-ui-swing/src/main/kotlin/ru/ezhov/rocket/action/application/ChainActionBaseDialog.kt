package ru.ezhov.rocket.action.application

import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.hotkey.HotKeyProviderSingleton
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.ui.utils.swing.common.MoveUtil
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities


class ChainActionBaseDialog(
    rocketActionContextFactory: RocketActionContextFactory,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
    private val configurationApplication: ConfigurationApplication,
) : JDialog() {
    init {
        val basePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder()
        }

        basePanel.add(chainBasePanelFactory.chainBasePanel, BorderLayout.CENTER)

        isUndecorated = true

        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.4F)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)

        registerGlobalHotKeys(chainBasePanelFactory = chainBasePanelFactory)

        val moveLabel = JLabel(rocketActionContextFactory.context.icon().by(AppIcon.MOVE))
        MoveUtil.addMoveAction(movableComponent = this, grabbedComponent = moveLabel)
        add(basePanel, BorderLayout.CENTER)
        add(moveLabel, BorderLayout.EAST)

        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }

    private fun registerGlobalHotKeys(
        chainBasePanelFactory: ChainBasePanelFactory
    ) {
        HotKeyProviderSingleton
            .PROVIDER
            .apply {
                configurationApplication
                    .all()
                    .globalHotKeys
                    ?.activateChainActionField
                    ?.let { activateChainActionField ->
                        this.register(KeyStroke.getKeyStroke(activateChainActionField)) {
                            SwingUtilities.invokeLater {
                                BaseDialog@ toFront()
                                BaseDialog@ requestFocusInWindow()
                                chainBasePanelFactory.chainBasePanel.getSearchTextField().requestFocusInWindow()
                            }
                        }
                    }
            }
    }
}
