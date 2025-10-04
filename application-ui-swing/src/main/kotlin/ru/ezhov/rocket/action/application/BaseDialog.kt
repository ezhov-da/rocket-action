package ru.ezhov.rocket.action.application

import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog


class BaseDialog(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
) : JDialog() {
    init {
        val menuAndSearch = uiQuickActionService.createMenu(this)
        jMenuBar = menuAndSearch.menu
        isUndecorated = true
        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.4F)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)
        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }
}
