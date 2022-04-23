package ru.ezhov.rocket.action.application.new_.domain.model

class ActionSettings(
    val id: ActionId,
    val map: Map<ActionSettingName, ActionSettingValue?>,
)
