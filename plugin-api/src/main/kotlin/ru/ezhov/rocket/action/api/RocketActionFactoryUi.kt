package ru.ezhov.rocket.action.api

/**
 * Фабрика отвечающая за создание действия
 */
interface RocketActionFactoryUi {
    /**
     * Действие должно создаваться только после вызова этого метода.
     * Необходимо "тяжёлые" действия по созданию компонента производить в другом
     * потоке и не блокировать UI.
     *
     * @return действие
     */
    fun create(settings: RocketActionSettings): RocketAction?

    fun type(): RocketActionType
}
