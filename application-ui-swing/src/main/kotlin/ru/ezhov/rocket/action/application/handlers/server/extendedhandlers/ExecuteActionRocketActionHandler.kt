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
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction

private const val COMMAND_NAME = "execute-action"
private const val ACTION_ID_KEY = "_id"
private const val PARAM_KEY = "text"
private const val RESULT_KEY = "result"

@Component
class ExecuteActionRocketActionHandler(
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val actionExecutor: ActionExecutor
) : ExtendedRocketActionHandler {
    override fun id(): String = "48344b6d-cea7-4460-8c0e-7824d661d10e"

    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
        object : RocketActionHandlerCommandContract {
            override fun commandName(): String = COMMAND_NAME

            override fun title(): String = "Execute chain or atomic action"

            override fun description(): String = "Execute chain or atomic action"

            override fun inputArguments(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey(ACTION_ID_KEY)

                    override fun name(): String = "Action ID"

                    override fun description(): String = "Action ID"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                },
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(PARAM_KEY)

                    override fun name(): String = "Action parameter"

                    override fun description(): String = "Action parameter"

                    override fun isRequired(): Boolean = false

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                },
            )

            override fun outputParams(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey(RESULT_KEY)

                    override fun name(): String = "Result"

                    override fun description(): String = "Result"

                    override fun isRequired(): Boolean = false

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()
                }
            )

        }
    )

    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
        return if (command.commandName == COMMAND_NAME) {
            val id = command.arguments[ACTION_ID_KEY]
                ?: return RocketActionHandleStatus.InvalidInputData(listOf("Parameter '$ACTION_ID_KEY' is required"))

            val text = command.arguments[PARAM_KEY]

            val action = chainActionService.byId(id) ?: atomicActionService.atomicBy(id)
            ?: return RocketActionHandleStatus.InvalidInputData(listOf("Action by ID '$id' is not found"))

            var executeResult: RocketActionHandleStatus? = null

            actionExecutor.execute(
                input = text,
                action = action,
                progressExecutingAction = object : ProgressExecutingAction {
                    override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                        executeResult = if (result == null) {
                            RocketActionHandleStatus.Success()
                        } else {
                            RocketActionHandleStatus.Success(mapOf(RESULT_KEY to result.toString()))

                        }
                    }

                    override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                        // not interesting
                    }

                    override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                        executeResult = RocketActionHandleStatus.Error(
                            "Error when execute atomic action with ID '${atomicAction?.id}'", ex
                        )
                    }
                }
            )

            executeResult!!

        } else {
            RocketActionHandleStatus.Error("Wrong command. Support only '$COMMAND_NAME'")
        }
    }
}
