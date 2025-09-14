package ru.ezhov.rocket.action.application

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.hotkey.HotKeyProviderSingleton
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities

class BaseDialog(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
    private val configurationApplication: ConfigurationApplication,
) : JDialog() {
    init {
        val basePanel = JPanel(MigLayout(/*"debug"*/"insets 0"))

        val menuAndSearch = uiQuickActionService.createMenu(this) { state ->
            when (state) {
                WindowState.MAXIMISE -> {
                    SwingUtilities.invokeLater {
                        basePanel.isVisible = true
                        pack()
                    }
                }

                WindowState.MINIMISE -> {
                    SwingUtilities.invokeLater {
                        basePanel.isVisible = false
                        pack()
                    }
                }
            }
        }

        jMenuBar = menuAndSearch.menu

        val baseDialogWidth = generalPropertiesRepository.asInteger(UsedPropertiesName.UI_BASE_DIALOG_WIDTH, 40)

        if (generalPropertiesRepository.asBoolean(UsedPropertiesName.CHAIN_ACTION_ENABLE, false)) {
            // growx 0 не должно расти по ширине
            basePanel.add(menuAndSearch.search.component, "wrap")
            basePanel.add(chainBasePanelFactory.chainBasePanel, "width ${baseDialogWidth}px")
        } else {
            basePanel.add(menuAndSearch.search.component, "width ${baseDialogWidth}px")
        }

        add(basePanel, BorderLayout.CENTER)

        isUndecorated = true

        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.7F)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)

        registerGlobalHotKeys(menuAndSearch = menuAndSearch, chainBasePanelFactory = chainBasePanelFactory)

        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }

    private fun registerGlobalHotKeys(
        menuAndSearch: ManuAndSearchPanel,
        chainBasePanelFactory: ChainBasePanelFactory
    ) {
        HotKeyProviderSingleton
            .PROVIDER
            .apply {
                configurationApplication
                    .all()
                    .globalHotKeys
                    ?.activateSearchField
                    ?.let { activateSearchField ->
                        this.register(KeyStroke.getKeyStroke(activateSearchField)) {
                            SwingUtilities.invokeLater {
                                BaseDialog@ toFront()
                                BaseDialog@ requestFocusInWindow()
                                menuAndSearch.search.searchTextField.requestFocusInWindow()
                            }
                        }
                    }

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
