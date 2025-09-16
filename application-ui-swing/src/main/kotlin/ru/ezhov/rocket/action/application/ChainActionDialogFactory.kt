package ru.ezhov.rocket.action.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.ChainBasePanelFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName

@Service
class ChainActionDialogFactory(
    generalPropertiesRepository: GeneralPropertiesRepository,
    chainBasePanelFactory: ChainBasePanelFactory,
    configurationApplication: ConfigurationApplication,
    rocketActionContextFactory: RocketActionContextFactory,
) : InitializingBean {
    val dialog: ChainActionBaseDialog? =
        if (generalPropertiesRepository.asBoolean(UsedPropertiesName.CHAIN_ACTION_ENABLE, false)) {
            ChainActionBaseDialog(
                generalPropertiesRepository = generalPropertiesRepository,
                chainBasePanelFactory = chainBasePanelFactory,
                configurationApplication = configurationApplication,
                rocketActionContextFactory = rocketActionContextFactory,
            )
        } else {
            null
        }

    companion object {
        var INSTANCE: ChainActionBaseDialog? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = dialog
    }
}
