package ru.ezhov.rocket.action.api.context

import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.api.context.variables.VariablesService

/**
 * Вспомогательный контекст предоставляющий функционал для удобного создания действия
 */
interface RocketActionContext {
    /**
     * Сервис по работе с иконками
     */
    fun icon(): IconService

    /**
     * Сервис по работе с уведомлениями
     */
    fun notification(): NotificationService

    /**
     * Сервис кеширования
     */
    fun cache(): CacheService

    /**
     * Сервис предоставления переменных
     */
    fun variables(): VariablesService = object : VariablesService {
        override fun variables(): Map<String, String> = emptyMap()
    }

    /**
     * Сервис регистрации для поиска
     */
    fun search(): Search
}
