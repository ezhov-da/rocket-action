package ru.ezhov.rocket.action.application

import net.miginfocom.swing.MigLayout
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JDialog
import javax.swing.JPanel


class BaseDialog(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
) : JDialog() {
    init {
        val menuAndSearch = uiQuickActionService.createMenu(this)

        // Добавление панели отображения прогресса
        val basePanel = JPanel(MigLayout(/*"debug"*/"insets 0 0 5 0" /*Убираем отступы, оставляем только снизу для отображения действий*/)).apply {
            border = BorderFactory.createEmptyBorder()
            add(menuAndSearch.executeStatusPanel, "hmax 6, width 100%, hidemode 2")
        }

        jMenuBar = menuAndSearch.menu
        isUndecorated = true
        opacity = generalPropertiesRepository
            .asFloat(name = UsedPropertiesName.UI_BASE_DIALOG_OPACITY, default = 0.4F)

        add(basePanel, BorderLayout.CENTER)

        rootPane.border = BorderFactory.createLineBorder(Color.GRAY)
        isAlwaysOnTop = true
        setLocationRelativeTo(null)
        pack()
    }
}
