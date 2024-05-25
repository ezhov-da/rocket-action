package ru.ezhov.rocket.action.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository

@Service
class BaseDialogFactory(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
) : InitializingBean {
    val dialog: BaseDialog = BaseDialog(
        uiQuickActionService,
        generalPropertiesRepository,
        chainBasePanelFactory,
    )

    companion object {
        var INSTANCE: BaseDialog? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = dialog
    }
}
