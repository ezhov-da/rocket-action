package ru.ezhov.rocket.action.api

import java.awt.Component

/**
 * Созданное и готовое к работе действие
 */
interface RocketAction {
    /**
     * Определяет необходимость отображения этого действия в результатах поиска

     * @return true если действие необходимо отразить в результатах поиска
     */
    fun contains(search: String): Boolean

    /**
     * Изменились ли настройки действия и нужно ли его пересоздать.
     * Используется для кеширования действия
     */
    fun isChanged(actionSettings: RocketActionSettings): Boolean

    /**
     * Компонент действия для отображения.
     * Важно. Данный метод не должен создавать компонент.
     * Должен возвращаться компонент созданный методом [RocketActionFactoryUi.create].
     */
    fun component(): Component
}
