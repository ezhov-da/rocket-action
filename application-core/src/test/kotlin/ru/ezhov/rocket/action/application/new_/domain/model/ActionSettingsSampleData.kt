package ru.ezhov.rocket.action.application.new_.domain.model

object ActionSettingsSampleData {
    fun default(
        id: ActionId = ActionIdSampleData.default(),
        map: Map<ActionSettingName, ActionSettingValue?> = mapOf(
            ActionSettingName(value = "name") to ActionSettingValue(value = "value")
        ),
    ) = ActionSettings(id = id, map = map)
}
