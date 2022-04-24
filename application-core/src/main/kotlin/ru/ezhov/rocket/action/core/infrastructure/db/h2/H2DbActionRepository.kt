package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import org.ktorm.dsl.delete
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.dsl.isNull
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.map
import org.ktorm.entity.toCollection
import org.ktorm.entity.update
import ru.ezhov.rocket.action.core.domain.repository.ActionRepository
import ru.ezhov.rocket.action.core.domain.repository.GetActionRepositoryException
import ru.ezhov.rocket.action.core.domain.repository.GetChildrenActionRepositoryException
import ru.ezhov.rocket.action.core.domain.repository.RemoveActionRepositoryException
import ru.ezhov.rocket.action.core.domain.repository.SaveActionRepositoryException
import ru.ezhov.rocket.action.core.domain.model.Action
import ru.ezhov.rocket.action.core.domain.model.ActionId
import ru.ezhov.rocket.action.core.domain.model.ActionOrder
import ru.ezhov.rocket.action.core.domain.model.ActionType
import ru.ezhov.rocket.action.core.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.core.infrastructure.db.database
import ru.ezhov.rocket.action.core.infrastructure.db.h2.dto.ActionEntity
import ru.ezhov.rocket.action.core.infrastructure.db.h2.schema.ActionTable

class H2DbActionRepository(private val factory: KtormDbConnectionFactory) : ActionRepository {
    override fun action(id: ActionId): Either<GetActionRepositoryException, Action?> =
        factory.database { e ->
            GetActionRepositoryException(
                message = "Error get connection when get action by id=${id.value}",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions
                            .find { it.id eq id.value }
                            ?.toDomainModel()
                    )
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get action by id=${id.value}", e))
                }
            }

    private fun ActionEntity.toDomainModel() = Action.restore(
        id = ActionId(id),
        type = ActionType(type),
        order = ActionOrder(order),
        creationDate = creationDate,
        updateDate = updateDate,
        parentId = parentId?.let { ActionId(it) },
    )

    override fun actions(ids: List<ActionId>): Either<GetActionRepositoryException, List<Action>> =
        factory.database { e ->
            GetActionRepositoryException(
                message = "Error get connection when get actions",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions
                            .filter { it.id inList ids.map { uuid -> uuid.value } }
                            .toCollection(ArrayList()).map { ae -> ae.toDomainModel() }

                    )
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get actions", e))
                }
            }

    override fun all(): Either<GetActionRepositoryException, List<Action>> =
        factory.database { e ->
            GetActionRepositoryException(
                message = "Error get connection when get all actions",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions.toCollection(ArrayList()).map { ae -> ae.toDomainModel() }
                    )
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get all actions", e))
                }
            }

    override fun addOrUpdate(actions: List<Action>): Either<SaveActionRepositoryException, Unit> =
        factory.database { e ->
            SaveActionRepositoryException(
                message = "Error get connection when save action id=${actions.map { it.id.value }}",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    actions.forEach { action ->
                        val existsAction = db.actions
                            .filter { it.id eq action.id.value }
                            .map { it.toDomainModel() }
                            .firstOrNull()
                        val actionEntity = action.toDbModel()
                        existsAction?.let {
                            db.actions.update(actionEntity)
                        }
                            ?: run {
                                db.actions.add(actionEntity)
                            }
                    }

                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(
                        SaveActionRepositoryException(
                            message = "Error when save action by id=${actions.map { it.id.value }}",
                            cause = e
                        )
                    )
                }
            }

    private fun Action.toDbModel(): ActionEntity {
        val action = this
        return ActionEntity {
            this.id = action.id.value
            this.type = action.type.value
            this.order = action.order.value
            this.creationDate = action.creationDate
            this.updateDate = action.updateDate
            this.parentId = action.parentId?.value
        }
    }

    override fun children(id: ActionId?): Either<GetChildrenActionRepositoryException, List<Action>> =
        factory.database { e ->
            GetChildrenActionRepositoryException(
                message = "Error get connection when get action children by id=${id?.value}",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        id
                            ?.let { parentId ->
                                db.actions
                                    .filter { it.parentId eq parentId.value }
                                    .map { it.toDomainModel() }
                            }
                            ?: run {
                                db.actions
                                    .filter { it.parentId.isNull() }
                                    .map { it.toDomainModel() }
                            }
                    )
                } catch (e: Exception) {
                    Either.Left(
                        GetChildrenActionRepositoryException(
                            message = "Error when get action children by id=${id?.value}",
                            cause = e
                        )
                    )
                }
            }

    override fun remove(ids: List<ActionId>): Either<RemoveActionRepositoryException, Unit> =
        factory.database { e ->
            RemoveActionRepositoryException(
                message = "Error get connection when delete actions ids=${ids.map { it.value }.joinToString()}",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    db.useTransaction {
                        db.delete(ActionTable) { it.id inList ids.map { uuids -> uuids.value } }
                    }
                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(
                        RemoveActionRepositoryException(
                            message = "Error when delete actions by id=${ids.map { it.value }.joinToString()}",
                            cause = e
                        )
                    )
                }
            }
}
