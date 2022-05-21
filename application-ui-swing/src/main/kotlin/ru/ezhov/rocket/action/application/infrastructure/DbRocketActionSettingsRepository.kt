package ru.ezhov.rocket.action.application.infrastructure

import arrow.core.getOrHandle
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.core.application.get.GetActionApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionSettingsApplicationService
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import java.util.UUID

private val logger = KotlinLogging.logger {}

class DbRocketActionSettingsRepository(
    private val getActionApplicationService: GetActionApplicationService,
    private val getActionSettingsApplicationService: GetActionSettingsApplicationService,
) : RocketActionSettingsRepository {
    override fun actions(): List<RocketActionSettings> {
        val actions = getActionApplicationService
            .all()
            .getOrHandle { throw Exception(it) }
            .sortedWith(compareBy<Action, String>(nullsLast()) { it.parentId?.value.toString() }
                .thenComparing { v -> v.order.value })
        val actionSettings: Map<UUID, List<ActionSettings>> = getActionSettingsApplicationService
            .all()
            .getOrHandle { throw Exception(it) }
            .groupBy { it.id.value }

        val rootActions = actions.filter { it.parentId == null }
            .map { it.toMutableRocketActionSettings(settings = actionSettings) }
            .toMutableList()

        val actionsMap = rootActions.groupBy { it.action.id.value }.toMutableMap()

        actions.forEach { action ->
            if (action.parentId != null) {
                val parentId = action.parentId!!
                val parent = actionsMap[parentId.value]?.firstOrNull()
                val m = action.toMutableRocketActionSettings(actionSettings)
                if (parent == null) {
                    rootActions.add(m)
                } else {
                    parent!!.add(m)
                }
                actionsMap[m.action.id.value] = listOf(m)
            }
        }

        return rootActions.map { it.to() }
    }

    private fun Action.toMutableRocketActionSettings(settings: Map<UUID, List<ActionSettings>>) =
        RocketActionSettingsNode(
            action = this,
            settings = settings[this.id.value]?.firstOrNull() ?: ActionSettings.empty(this.id),
        )

    override fun save(settings: List<RocketActionSettings>) {
        TODO("Not implemented")
    }
}
