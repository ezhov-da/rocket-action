package ru.ezhov.rocket.action.api

import ru.ezhov.rocket.action.api.context.RocketActionContext

/**
 * Фабрика отвечающая за создание действия
 */
interface RocketActionFactoryUi {
    /**
     * Действие должно создаваться только после вызова этого метода.
     * Необходимо "тяжёлые" действия по созданию компонента производить в другом
     * потоке и не блокировать UI.
     *
     * @param settings настройки действия
     * @param context контекст для создания действия
     * @return действие
     */
    fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction?

    fun type(): RocketActionType
}
