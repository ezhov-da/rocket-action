package ru.ezhov.rocket.action.application.availableproperties

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository

@Service
class AvailablePropertiesFromCommandLineDialogFactory(
    generalPropertiesRepository: GeneralPropertiesRepository
) {
    val properties: AvailablePropertiesFromCommandLineDialog = AvailablePropertiesFromCommandLineDialog()
}
