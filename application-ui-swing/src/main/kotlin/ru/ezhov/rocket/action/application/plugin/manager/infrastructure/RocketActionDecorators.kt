package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.handler.RocketActionHandleStatus
import ru.ezhov.rocket.action.api.handler.RocketActionHandler
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommand
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerCommandContract
import ru.ezhov.rocket.action.api.handler.RocketActionHandlerFactory
import java.awt.Component
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class RocketActionPluginDecorator(
    private val rocketActionPluginOriginal: RocketActionPlugin,
) : RocketActionPlugin {
    override fun factory(context: RocketActionContext): RocketActionFactoryUi = RocketActionFactoryUiDecorator(
        rocketActionFactoryUi = rocketActionPluginOriginal.factory(context = context)
    )

    override fun configuration(context: RocketActionContext): RocketActionConfiguration =
        rocketActionPluginOriginal.configuration(context)
}

class RocketActionFactoryUiDecorator(
    private val rocketActionFactoryUi: RocketActionFactoryUi
) : RocketActionFactoryUi {
    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        rocketActionFactoryUi.create(settings = settings, context = context)
            ?.let { ra ->
                when (val handlerFactory = ra as? RocketActionHandlerFactory) {
                    null -> RocketActionDecorator(originalRocketAction = ra)
                    else -> handlerFactory.handler()
                        ?.let { handler ->
                            RocketActionAndHandlerDecorator(
                                originalRocketAction = ra,
                                originalRocketActionHandler = handler,
                            )
                        }
                        ?: run {
                            logger.info {
                                "${ra.javaClass.name} implement " +
                                    "${RocketActionHandlerFactory::class.java.name}, " +
                                    "but handler is null"
                            }
                            RocketActionDecorator(originalRocketAction = ra)
                        }
                }
            }

    override fun type(): RocketActionType = rocketActionFactoryUi.type()
}

open class RocketActionDecorator(
    private val originalRocketAction: RocketAction
) : RocketAction {
    companion object {
        const val MAX_TIME_GET_COMPONENT_IN_MILLS = 2
    }

    override fun contains(search: String): Boolean = originalRocketAction.contains(search = search)

    override fun isChanged(actionSettings: RocketActionSettings): Boolean =
        originalRocketAction.isChanged(actionSettings = actionSettings)

    override fun component(): Component {
        val component: Component
        val timeInMillis = measureTimeMillis {
            component = originalRocketAction.component()
        }

        if (timeInMillis > MAX_TIME_GET_COMPONENT_IN_MILLS) {
            logger.warn {
                "Getting component for action was over '$MAX_TIME_GET_COMPONENT_IN_MILLS' milliseconds. " +
                    "This can slow down the application"
            }
        }

        return component
    }
}

class RocketActionAndHandlerDecorator(
    private val originalRocketActionHandler: RocketActionHandler,
    originalRocketAction: RocketAction,
) : RocketActionDecorator(originalRocketAction), RocketActionHandler {
    override fun id(): String = originalRocketActionHandler.id()

    override fun contracts(): List<RocketActionHandlerCommandContract> = originalRocketActionHandler.contracts()

    override fun handle(command: RocketActionHandlerCommand): RocketActionHandleStatus =
        originalRocketActionHandler.handle(command = command)
}
