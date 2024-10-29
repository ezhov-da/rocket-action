package ru.ezhov.rocket.action.application

import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog

class BaseDialog(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
) : JDialog() {
    init {
        val menuAndSearch = uiQuickActionService.createMenu(this)

        jMenuBar = menuAndSearch.menu

        if (generalPropertiesRepository.asBoolean(UsedPropertiesName.CHAIN_ACTION_ENABLE, false)) {
            add(menuAndSearch.search, BorderLayout.NORTH)
            add(chainBasePanelFactory.chainBasePanel, BorderLayout.CENTER)
        }

        isUndecorated = true

        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.7F)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)

        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }
}
