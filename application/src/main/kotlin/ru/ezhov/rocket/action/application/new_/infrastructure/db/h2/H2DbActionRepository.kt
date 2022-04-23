package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.application.new_.domain.ActionRepository
import ru.ezhov.rocket.action.application.new_.domain.GetActionRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.Action
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.new_.domain.model.ActionType
import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbConnectionFactory
import java.util.UUID

class H2DbActionRepository(private val factory: DbConnectionFactory) : ActionRepository {
    override fun action(id: ActionId): Either<GetActionRepositoryException, Action?> =
        factory.connection()
            .handleErrorWith { ex ->
                Either.Left(
                    GetActionRepositoryException(
                        message = "Error get connection when get action by id=${id.value}",
                        cause = ex
                    )
                )
            }
            .flatMap { connection ->
                try {
                    connection.use { c ->
                        c.prepareStatement(
                            """
                                SELECT
                                    ID,
                                    "TYPE",
                                    CREATION_DATE,
                                    UPDATE_DATE,
                                    "ORDER",
                                    PARENT_ID
                                FROM ACTION
                                WHERE ID = ?""".trimIndent()
                        )
                            .use { ps ->
                                ps.setObject(1, id.value)
                                ps.executeQuery()
                                    .use { rs ->
                                        while (rs.next()) {
                                            val id = rs.getObject(1) as UUID
                                            val type = rs.getString(2)
                                            val creationDate = rs.getTimestamp(3).toLocalDateTime()
                                            val updateDate = rs.getTimestamp(4)?.toLocalDateTime()
                                            val order = rs.getInt(5)
                                            val parentId = rs.getObject(6) as? UUID
                                            return Either.Right(
                                                Action.restore(
                                                    id = ActionId(id),
                                                    type = ActionType(type),
                                                    order = ActionOrder(order),
                                                    creationDate = creationDate,
                                                    updateDate = updateDate,
                                                    parentId = parentId?.let { ActionId(it) },
                                                )
                                            )
                                        }

                                        return Either.Right(null)
                                    }
                            }
                    }
                } catch (e: Exception) {
                    Either.Left(GetActionRepositoryException("Error when get action by id=${id.value.toString()}", e))
                }
            }


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
