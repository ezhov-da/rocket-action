package ru.ezhov.rocket.action.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository

@Service
class BaseDialogFactory(
    uiQuickActionService: UiQuickActionService,
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
    configurationApplication: ConfigurationApplication,
) : InitializingBean {
    val dialog: BaseDialog = BaseDialog(
        uiQuickActionService = uiQuickActionService,
        generalPropertiesRepository = generalPropertiesRepository,
        chainBasePanelFactory = chainBasePanelFactory,
        configurationApplication = configurationApplication
    )

    companion object {
        var INSTANCE: BaseDialog? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = dialog
    }
}
