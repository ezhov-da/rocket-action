package ru.ezhov.rocket.action.application.handlers.server.extendedhandlers

import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerProperty
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertyKey
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerPropertySpec
import ru.ezhov.rocket.action.application.core.application.CreateRocketActionSettingsService

private const val GROUP_ID_KEY = "_groupId"
private const val TYPE_KEY = "_type"
private const val TAGS_KEY = "_tags"

@Component
class CreateRocketActionHandler(
    private val createRocketActionSettingsService: CreateRocketActionSettingsService
) : ExtendedRocketActionHandler {
    override fun id(): String = "6f6f97ce-710f-49a2-90a9-c14f0408a4f9"

    override fun contracts(): List<RocketActionHandlerCommandContract> = listOf(
        object : RocketActionHandlerCommandContract {
            override fun commandName(): String = "create-rocket-action"

            override fun title(): String = "Create Rocket Action"

            override fun description(): String = "Create Rocket Action in exists Group"

            override fun inputArguments(): List<RocketActionHandlerProperty> = listOf(
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey(GROUP_ID_KEY)

                    override fun name(): String = "Group ID"

                    override fun description(): String = "Group ID"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                },
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey =
                        RocketActionHandlerPropertyKey(TYPE_KEY)

                    override fun name(): String = "Rocket action type"

                    override fun description(): String = "Rocket action type"

                    override fun isRequired(): Boolean = true

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                },
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey(TAGS_KEY)

                    override fun name(): String = "Tags by "

                    override fun description(): String = "Tags separated by commas"

                    override fun isRequired(): Boolean = false

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                },
                object : RocketActionHandlerProperty {
                    override fun key(): RocketActionHandlerPropertyKey = RocketActionHandlerPropertyKey("keyAction")

                    override fun name(): String = "Property for action"

                    override fun description(): String = "Any property for action"

                    override fun isRequired(): Boolean = false

                    override fun property(): RocketActionHandlerPropertySpec =
                        RocketActionHandlerPropertySpec.StringPropertySpec()

                }
            )

            override fun outputParams(): List<RocketActionHandlerProperty> = emptyList()

        }
    )

    // TODO ezhov прочесать
    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus {
        val mutableArguments = command.arguments.toMutableMap()

        val groupId = mutableArguments[GROUP_ID_KEY]
        val tags = mutableArguments[TAGS_KEY]
        val rocketActionType = mutableArguments[TYPE_KEY]

        mutableArguments.remove(GROUP_ID_KEY)
        mutableArguments.remove(TYPE_KEY)
        mutableArguments.remove(TAGS_KEY)

        return try {
            if (groupId == null || rocketActionType == null || mutableArguments.isEmpty()) {
                throw IllegalArgumentException(
                    "$GROUP_ID_KEY '$groupId', $TYPE_KEY '$rocketActionType', properties '$mutableArguments' is required"
                )
            }

            createRocketActionSettingsService.create(
                groupId = groupId,
                type = rocketActionType,
                params = mutableArguments,
                tags = tags
            )

            RocketActionHandleStatus.Success()
        } catch (ex: Exception) {
            RocketActionHandleStatus.Error("Error when create rocket action from '$command' in handler", ex)
        }
    }
}
