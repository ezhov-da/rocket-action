package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbConnectionFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object H2DbConnectionFactorySampleData {
    fun factory(
        url: String = "jdbc:h2:./../db/test/rocket-action/rocket-action",
        user: String = "rocket-action",
        password: String = "rocket-action",
    ): DbConnectionFactory = object : DbConnectionFactory {
        override fun connection(): Either<SQLException, Connection> =
            try {
                Either.Right(DriverManager.getConnection(url, user, password))
            } catch (e: SQLException) {
                Either.Left(e)
            }
    }
}
