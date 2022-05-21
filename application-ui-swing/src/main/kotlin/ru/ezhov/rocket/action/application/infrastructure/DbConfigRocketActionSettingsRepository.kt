package ru.ezhov.rocket.action.application.infrastructure

import arrow.core.Either
import arrow.core.getOrHandle
import arrow.core.handleErrorWith
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.domain.ConfigRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.core.application.change.ChangeActionApplicationService
import ru.ezhov.rocket.action.core.application.create.CreateActionApplicationService
import ru.ezhov.rocket.action.core.application.delete.DeleteActionApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionApplicationService
import ru.ezhov.rocket.action.core.application.get.GetActionSettingsApplicationService
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionOrder
import ru.ezhov.rocket.action.core.domain.model.ActionSettingName
import ru.ezhov.rocket.action.core.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.core.domain.model.ActionSettings
import ru.ezhov.rocket.action.core.domain.model.ActionType
import ru.ezhov.rocket.action.core.domain.model.NewAction
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

class DbConfigRocketActionSettingsRepository(
    private val getActionApplicationService: GetActionApplicationService,
    private val getActionSettingsApplicationService: GetActionSettingsApplicationService,
    private val createActionApplicationService: CreateActionApplicationService,
    private val deleteActionApplicationService: DeleteActionApplicationService,
    private val changeActionApplicationService: ChangeActionApplicationService,
) : ConfigRocketActionSettingsRepository {
    override fun actions(): List<RocketActionSettingsNode> {
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

        val actionsMap = rootActions.groupBy { it.action.id.toString() }.toMutableMap()

        actions.forEach { action ->
            if (action.parentId != null) {
                val parentId = action.parentId!!
                val parent = actionsMap[parentId.value.toString()]?.firstOrNull()
                val m = action.toMutableRocketActionSettings(actionSettings)
                if (parent == null) {
                    rootActions.add(m)
                } else {
                    parent!!.add(m)
                }
                actionsMap[m.action.id.toString()] = listOf(m)
            }
        }

        return rootActions
    }


    private fun Action.toMutableRocketActionSettings(settings: Map<UUID, List<ActionSettings>>) =
        RocketActionSettingsNode(
            action = this,
            settings = settings[this.id.value]?.firstOrNull() ?: ActionSettings.empty(this.id),
        )

    override fun create(
        settings: RocketActionSettings,
        afterId: String?,
    ): Either<RocketActionSettingsRepositoryException, Unit> =
        if (afterId == null) {
            Either.Right(null)
        } else {
            getActionApplicationService
                .action(ActionId.of(afterId))
        }
            .handleErrorWith { Either.Left(RocketActionSettingsRepositoryException("Error", it)) }
            .map { create(settings = settings, afterAction = it) }

    private fun create(
        settings: RocketActionSettings,
        afterAction: Action? = null,
    ): Either<RocketActionSettingsRepositoryException, Unit> {
        val newAction = NewAction.create(
            id = ActionId(UUID.fromString(settings.id())),
            type = ActionType(settings.type().value()),
            order = afterAction?.let { it.order.plusOne() } ?: ActionOrder(1),
            creationDate = LocalDateTime.now(),
            parentId = afterAction?.let { it.parentId },
            map = settings.settings()
                .entries
                .associate { ActionSettingName(it.key.value) to ActionSettingValue(it.key.value) }
        )
        return createActionApplicationService.`do`(newAction)
            .handleErrorWith { Either.Left(RocketActionSettingsRepositoryException("error", it)) }
    }

    override fun delete(id: ActionId): Either<RocketActionSettingsRepositoryException, Unit> =
        deleteActionApplicationService.`do`(id = id, withAllChildrenRecursive = true)
            .handleErrorWith { Either.Left(RocketActionSettingsRepositoryException("error", it)) }

    override fun before(id: ActionId, beforeId: ActionId): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun after(id: ActionId, afterId: ActionId): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }
}
