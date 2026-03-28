package ru.ezhov.rocket.action.application.handlers.server.extendedhandlers

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService

private const val COMMAND_NAME = "all-chain-actions"
private const val RESULT_SETTINGS_KEY = "actions"

@Component
class AllActionsRocketActionHandler(
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : ExtendedRocketActionHandler {
    override fun id(): String = "40f70340-1c0a-4d1e-a9c5-edd69bd9b600"

    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
        object : RocketActionHandlerCommandContract {
            override fun commandName(): String = COMMAND_NAME

            override fun title(): String = "All chain actions"

            override fun description(): String = "All chain actions"

            override fun inputArguments(): List<RocketActionHandlerProperty> = emptyList()

            override fun outputParams(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(RESULT_SETTINGS_KEY)

                    override fun name(): String = "Actions"

                    override fun description(): String = "Actions as JSON"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()
                }
            )

        }
    )

    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
        return if (command.commandName == COMMAND_NAME) {
            data class Response(
                val id: String,
                val name: String,
                val description: String,
            )

            val chains = chainActionService.chains()
                .map { Response(id = it.id(), it.name(), it.description()) }
            val atomics = atomicActionService.atomics()
                .map { Response(id = it.id(), it.name(), it.description()) }

            RocketActionHandleStatus.Success(mapOf(RESULT_SETTINGS_KEY to chains + atomics))
        } else {
            RocketActionHandleStatus.Error("Wrong command. Support only '$COMMAND_NAME'")
        }
    }
}

