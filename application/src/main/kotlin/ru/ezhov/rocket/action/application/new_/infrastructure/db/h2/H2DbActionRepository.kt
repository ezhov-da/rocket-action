package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.map
import org.ktorm.entity.toCollection
import ru.ezhov.rocket.action.application.new_.domain.ActionRepository
import ru.ezhov.rocket.action.application.new_.domain.GetActionRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.GetChildrenActionRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionType
import ru.ezhov.rocket.action.application.new_.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.application.new_.infrastructure.db.database
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionEntity

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

    override fun actions(): Either<GetActionRepositoryException, List<Action>> =
        factory.database { e ->
            GetActionRepositoryException(
                message = "Error get connection when get actions",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions.toCollection(ArrayList()).map { ae -> ae.toDomainModel() }
                    )
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get actions", e))
                }
            }

    override fun save(action: Action): Either<GetActionRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun children(id: ActionId): Either<GetChildrenActionRepositoryException, List<Action>> =
        factory.database { e ->
            GetChildrenActionRepositoryException(
                message = "Error get connection when get action children by id=${id.value}",
                cause = e
            )
        }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions
                            .filter { it.parentId eq id.value }
                            .map { it.toDomainModel() }
                    )
                } catch (e: Exception) {
                    Either.Left(
                        GetChildrenActionRepositoryException(
                            message = "Error when get action children by id=${id.value}",
                            cause = e
                        )
                    )
                }
            }
}
