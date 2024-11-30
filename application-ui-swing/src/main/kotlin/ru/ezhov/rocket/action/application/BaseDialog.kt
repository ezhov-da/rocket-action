package ru.ezhov.rocket.action.application

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JPanel
import javax.swing.SwingUtilities

class BaseDialog(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
) : JDialog() {
    init {
        val basePanel = JPanel(MigLayout(/*"debug"*/ "insets 0"))

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

        val baseDialogWidth = generalPropertiesRepository.asInteger(UsedPropertiesName.UI_BASE_DIALOG_WIDTH, 130)

        if (generalPropertiesRepository.asBoolean(UsedPropertiesName.CHAIN_ACTION_ENABLE, false)) {
            // growx 0 не должно расти по ширине
            basePanel.add(menuAndSearch.search, "width ${baseDialogWidth}px, wrap")
            basePanel.add(chainBasePanelFactory.chainBasePanel, "width ${baseDialogWidth}px")
        } else {
            basePanel.add(menuAndSearch.search, "width ${baseDialogWidth}px")
        }

        add(basePanel, BorderLayout.CENTER)

        isUndecorated = true

        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.7F)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)

        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }
}
