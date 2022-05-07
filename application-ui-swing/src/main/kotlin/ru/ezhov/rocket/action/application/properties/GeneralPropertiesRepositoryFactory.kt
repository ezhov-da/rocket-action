package ru.ezhov.rocket.action.application.properties

object GeneralPropertiesRepositoryFactory {
    val repository: GeneralPropertiesRepository = CommandLineAndResourceGeneralPropertiesRepository()
}
