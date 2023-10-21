package ru.ezhov.rocket.action.application.configuration.ui.event.model

import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings

class RemoveSettingUiEvent(
    val countChildrenRoot: Int,
    val treeRocketActionSettings: TreeRocketActionSettings,
) : ConfigurationUiEvent()
