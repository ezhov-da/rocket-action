package ru.ezhov.rocket.action.application.infrastructure

import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.model.NewAction
import java.time.LocalDateTime

class RocketActionSettingsNode(
    val action: Action,
    val settings: ActionSettings,
    val children: MutableList<RocketActionSettingsNode> = ArrayList()
) {

    fun add(settings: RocketActionSettingsNode) {
        children.add(settings)
    }

    fun createNewWithoutChildren(): NewAction =
        NewAction.create(
            id = ActionId.create(),
            type = action.type,
            order = action.order.plusOne(),
            creationDate = LocalDateTime.now(),
            parentId = action.parentId,
            map = settings.map,
        )

    fun to(): RocketActionSettings = object : RocketActionSettings {
        override fun id(): String = action.id.value.toString()

        override fun type(): RocketActionType = RocketActionType { action.type.value }

        override fun settings(): Map<RocketActionConfigurationPropertyKey, String> =
            settings?.map
                ?.entries
                ?.associate {
                    RocketActionConfigurationPropertyKey(it.key.value) to it.value?.value.orEmpty()
                }
                ?.toMutableMap()
                ?: mutableMapOf()

        override fun actions(): List<RocketActionSettings> = children.map { it.to() }
    }
}
