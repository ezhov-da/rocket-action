package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import ru.ezhov.rocket.action.application.new_.domain.ActionRepository
import ru.ezhov.rocket.action.application.new_.domain.GetActionRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionType
import ru.ezhov.rocket.action.application.new_.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionEntity

class H2DbActionRepository(private val factory: KtormDbConnectionFactory) : ActionRepository {
    override fun action(id: ActionId): Either<GetActionRepositoryException, Action?> =
        factory.database()
            .handleErrorWith { ex ->
                Either.Left(
                    GetActionRepositoryException(
                        message = "Error get connection when get action by id=${id.value}",
                        cause = ex
                    )
                )
            }
            .flatMap { db ->
                try {
                    Either.Right(
                        db.actions
                            .find { it.id eq id.value }
                            ?.let { ae -> ae.toDomainModel() }
                    )
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get action by id=${id.value.toString()}", e))
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

    override fun actions(): Either<GetActionRepositoryException, List<Action>> {
        TODO("Not yet implemented")
    }

    override fun save(action: Action): Either<GetActionRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun children(id: ActionId): Either<GetActionRepositoryException, List<Action>> {
        TODO("Not yet implemented")
    }
}
