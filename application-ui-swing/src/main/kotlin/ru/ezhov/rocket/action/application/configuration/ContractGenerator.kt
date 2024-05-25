package ru.ezhov.rocket.action.application.configuration

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty

object ContractGenerator {
    fun generateToMarkDown(properties: List<RocketActionConfigurationProperty>): String {
        return "### Contract for automation of work with the application  \n" +
            "| Key | Name |  \n" +
            "| --- | --- |  \n" +
            properties.joinToString(separator = "  \n") { "| **${it.key()}** | ${it.name()} |" }
    }
}
