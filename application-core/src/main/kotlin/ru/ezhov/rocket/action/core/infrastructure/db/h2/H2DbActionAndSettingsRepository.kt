package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.dsl.or
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.toCollection
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.NewAction
import ru.ezhov.rocket.action.core.domain.repository.ActionAndSettingsRepository
import ru.ezhov.rocket.action.core.domain.repository.AddActionAndSettingsRepositoryException
import ru.ezhov.rocket.action.core.domain.repository.RemoveActionAndSettingsRepositoryException
import ru.ezhov.rocket.action.core.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.core.infrastructure.db.database
import ru.ezhov.rocket.action.core.infrastructure.db.h2.dto.ActionEntity
import ru.ezhov.rocket.action.core.infrastructure.db.h2.dto.ActionSettingsEntity
import ru.ezhov.rocket.action.core.infrastructure.db.h2.schema.ActionSettingsTable
import ru.ezhov.rocket.action.core.infrastructure.db.h2.schema.ActionTable

class H2DbActionAndSettingsRepository(
    private val factory: KtormDbConnectionFactory
) : ActionAndSettingsRepository {
    override fun add(action: NewAction): Either<AddActionAndSettingsRepositoryException, Unit> =
        factory.database { e ->
            AddActionAndSettingsRepositoryException(
                message = "Error get connection when save actions and settings",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    db.useTransaction {
                        db.actions.add(action.toActionDbModel())
                        action.toSettingsDbModel().forEach { db.actionSettings.add(it) }
                    }

                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(AddActionAndSettingsRepositoryException("Error when get actions", e))
                }
            }

    override fun remove(
        id: ActionId, withAllChildrenRecursive: Boolean
    ): Either<RemoveActionAndSettingsRepositoryException, Unit> =
        factory.database { e ->
            RemoveActionAndSettingsRepositoryException(
                message = "Error get connection when remove actions and settings",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    db.useTransaction {
                        when (withAllChildrenRecursive) {
                            true -> {
                                val childrenActionId = mutableListOf<ActionId>()
                                recursiveActionChild(id, childrenActionId)
                                db.delete(ActionTable) {
                                    it.id eq id.value or (it.id inList childrenActionId.map { u -> u.value })
                                }
                                db.delete(ActionSettingsTable) {
                                    it.id eq id.value or (it.id inList childrenActionId.map { u -> u.value })
                                }
                            }
                            false -> {
                                db.delete(ActionTable) { it.id eq id.value }
                                db.delete(ActionSettingsTable) { it.id eq id.value }
                            }
                        }
                    }

                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(RemoveActionAndSettingsRepositoryException(
                        message = "Error when remove actions and settings",
                        cause = e
                    ))
                }
            }

    private fun recursiveActionChild(
        parentId: ActionId,
        children: MutableList<ActionId>
    ): Either<RemoveActionAndSettingsRepositoryException, Unit> =
        factory.database { e ->
            RemoveActionAndSettingsRepositoryException(
                message = "Error get connection when remove actions and settings",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    val childrenFromDb = db.actions
                        .filter { it.parentId eq parentId.value }
                        .toCollection(ArrayList())
                        .map { ActionId(it.id) }

                    children.addAll(childrenFromDb)

                    if (childrenFromDb.isNotEmpty()) {
                        childrenFromDb.forEach { recursiveActionChild(it, children) }
                    }
                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(RemoveActionAndSettingsRepositoryException("Error when get children actions", e))
                }
            }

    private fun NewAction.toActionDbModel(): ActionEntity =
        this.let { action ->
            ActionEntity {
                this.id = action.id.value
                this.type = action.type.value
                this.order = action.order.value
                this.creationDate = action.creationDate
                this.updateDate = null
                this.parentId = action.parentId?.value
            }
        }

    private fun NewAction.toSettingsDbModel(): List<ActionSettingsEntity> {
        val actionId = this.id.value
        return this.map.map { aso ->
            ActionSettingsEntity {
                this.id = actionId
                this.name = aso.key.value
                this.value = aso.value?.value
            }
        }
    }
}
