package ru.ezhov.rocket.action.application.handlers.server.extendedhandlers

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import java.util.*

private const val COMMAND_NAME = "execute-script"
private const val PARAM_KEY_SCRIPT = "script"
private const val PARAM_KEY_TEXT = "text"
private const val RESULT_KEY = "result"

@Component
class ExecuteGroovyScriptAsAtomicActionRocketActionHandler(
    private val actionExecutor: ActionExecutor
) : ExtendedRocketActionHandler {
    override fun id(): String = "70c5601c-396f-45e2-a052-af7c3f551dbb"

    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
        object : RocketActionHandlerCommandContract {
            override fun commandName(): String = COMMAND_NAME

            override fun title(): String = "Execute script as atomic action"

            override fun description(): String = "Execute script as atomic action"

            override fun inputArguments(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(PARAM_KEY_SCRIPT)

                    override fun name(): String = "Script"

                    override fun description(): String = "Script"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()
                },
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(PARAM_KEY_TEXT)

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
            val script = command.arguments[PARAM_KEY_SCRIPT]
                ?: return RocketActionHandleStatus.InvalidInputData(listOf("Parameter '$PARAM_KEY_SCRIPT' is required"))

            val text = command.arguments[PARAM_KEY_TEXT]

            var executeResult: RocketActionHandleStatus? = null

            val id = UUID.randomUUID()
            actionExecutor.execute(
                input = text,
                action = AtomicAction(
                    id = "rest-stub-$id",
                    name = "Dynamic Rest API atomic action $id",
                    description = "skipped",
                    contractType = ContractType.IN_OUT,
                    engine = AtomicActionEngine.GROOVY,
                    source = AtomicActionSource.TEXT,
                    data = script,
                    alias = null,
                    icon = null,
                ),
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
