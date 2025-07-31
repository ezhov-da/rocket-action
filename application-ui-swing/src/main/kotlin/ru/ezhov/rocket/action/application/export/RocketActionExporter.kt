package ru.ezhov.rocket.action.application.export

import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel

interface RocketActionExporter {
    fun export(actions: ActionsModel)
}
