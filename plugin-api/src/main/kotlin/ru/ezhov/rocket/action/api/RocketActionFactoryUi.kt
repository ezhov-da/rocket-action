package ru.ezhov.rocket.action.api

import ru.ezhov.rocket.action.api.context.RocketActionContext

/**
 * Factory responsible for creating the action
 */
interface RocketActionFactoryUi {
    /**
     * The action should only be created after this method has been called.
     * It is necessary to perform "heavy" actions to create a component in another thread and not block the UI.
     *
     * @param settings action settings
     * @param context context to create an action
     * @return action
     */
    fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction?

    fun type(): RocketActionType
}
