package ru.ezhov.rocket.action.core.domain.model

class ActionSettings(
    val id: ActionId,
    val map: Map<ActionSettingName, ActionSettingValue?> = emptyMap(),
) {
    companion object{
        fun empty(id: ActionId) = ActionSettings(id)
    }

    fun addOrChangeProperty(name: ActionSettingName, value: ActionSettingValue?): ActionSettings =
        with(map.toMutableMap()) {
            this[name] = value
            ActionSettings(id, this)
        }

    fun value(name: ActionSettingName): ActionSettingValue? = map[name]
}
