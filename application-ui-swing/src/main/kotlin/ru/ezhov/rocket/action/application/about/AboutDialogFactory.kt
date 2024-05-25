package ru.ezhov.rocket.action.application.about

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.BaseDialogFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository

@Service
class AboutDialogFactory(
    generalPropertiesRepository: GeneralPropertiesRepository
) {
    val about: AboutDialog = AboutDialog(
        generalPropertiesRepository,
    )
}
