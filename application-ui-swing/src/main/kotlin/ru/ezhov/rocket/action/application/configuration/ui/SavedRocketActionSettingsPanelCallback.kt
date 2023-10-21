package ru.ezhov.rocket.action.application.configuration.ui

import ru.ezhov.rocket.action.application.configuration.ui.tree.TreeRocketActionSettings

interface SavedRocketActionSettingsPanelCallback {
    fun saved(settings: TreeRocketActionSettings)
}
