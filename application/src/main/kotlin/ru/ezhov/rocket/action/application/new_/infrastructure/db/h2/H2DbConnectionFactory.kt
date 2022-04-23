package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbConnectionFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class H2DbConnectionFactory : DbConnectionFactory {
    override fun connection(): Either<SQLException, Connection> = try {
        Either.Right(
            DriverManager.getConnection(
                "jdbc:h2:./db/test/rocket-action/rocket-action",
                "rocket-action",
                "rocket-action"
            )
        )
    } catch (e: SQLException) {
        Either.Left(e)
    }
}
