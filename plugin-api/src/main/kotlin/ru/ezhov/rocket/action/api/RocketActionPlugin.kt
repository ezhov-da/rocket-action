package ru.ezhov.rocket.action.api

import ru.ezhov.rocket.action.api.context.RocketActionContext

/**
 * Плагин действия
 */
interface RocketActionPlugin {
    /**
     * Фабрика по созданию UI
     */
    fun factory(context: RocketActionContext): RocketActionFactoryUi

    /**
     * Конфигурация действия
     */
    fun configuration(context: RocketActionContext): RocketActionConfiguration
}
