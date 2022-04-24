package ru.ezhov.rocket.action.application.new_.domain.model

class ActionSettings(
    val id: ActionId,
    val map: Map<ActionSettingName, ActionSettingValue?>,
) {
    fun addOrChangeProperty(name: ActionSettingName, value: ActionSettingValue?): ActionSettings =
        with(map.toMutableMap()) {
            this[name] = value
            ActionSettings(id, this)
        }

    fun value(name: ActionSettingName): ActionSettingValue? = map[name]
}
