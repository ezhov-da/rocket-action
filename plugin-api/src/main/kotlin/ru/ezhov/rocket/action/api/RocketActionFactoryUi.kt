package ru.ezhov.rocket.action.api

/**
 * Фабрика отвечающая за создание действия
 */
interface RocketActionFactoryUi {
    /**
     * Действие должно создаваться только после вызова этого метода
     *
     * @return действие
     */
    fun create(settings: RocketActionSettings): RocketAction?

    fun type(): RocketActionType
}