package ru.ezhov.rocket.action.application.handlers.server.extendedhandlers

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.model.SearchParameters

private const val COMMAND_NAME = "search-rocket-action-settings"
private const val TYPES_KEY = "types"
private const val RESULT_SETTINGS_KEY = "settings"

@Component
class SearchRocketActionSettingsHandler(
    private val rocketActionSettingsService: RocketActionSettingsService,
) : ExtendedRocketActionHandler {
    override fun id(): String = "f277cf95-8132-4f35-b1ec-a83e1d3837a9"

    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
        object : RocketActionHandlerCommandContract {
            override fun commandName(): String = COMMAND_NAME

            override fun title(): String = "Search Rocket Action settings by parameters"

            override fun description(): String = "Search Rocket Action settings by parameters"

            override fun inputArguments(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey(TYPES_KEY)

                    override fun name(): String = "Types"

                    override fun description(): String = "Comma separated types. Example `OPEN_URL, TEST`"

                    override fun isRequired(): Boolean = false

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()
                },
            )

            override fun outputParams(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(RESULT_SETTINGS_KEY)

                    override fun name(): String = "Settings"

                    override fun description(): String = "Settings as JSON with id and contract fields in settings"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()
                }
            )

        }
    )

    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
        return if (command.commandName == COMMAND_NAME) {
            val types = command.arguments[TYPES_KEY]

            val settings =
                rocketActionSettingsService.searchBy(
                    parameters = SearchParameters(
                        types = types?.let { it.split(",").map { t -> t.trim() } }.orEmpty()
                    )
                ).map {
                    val map = mutableMapOf("_id" to it.id)
                    it.settings.forEach { s ->
                        map[s.name] = s.value
                    }

                    map
                }

            RocketActionHandleStatus.Success(mapOf(RESULT_SETTINGS_KEY to settings))
        } else {
            RocketActionHandleStatus.Error("Wrong command. Support only '$COMMAND_NAME'")
        }
    }
}
