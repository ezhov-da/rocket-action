package ru.ezhov.rocket.action.core.infrastructure.db

import arrow.core.Either
import ru.ezhov.rocket.action.core.infrastructure.db.DbConnectionFactory
import ru.ezhov.rocket.action.core.infrastructure.db.DbCredentialsFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DriverManagerConnectionFactory(private val credentials: DbCredentialsFactory) : DbConnectionFactory {
    override fun connection(): Either<SQLException, Connection> = try {
        Either.Right(
            DriverManager.getConnection(
                credentials.url,
                credentials.user,
                credentials.password
            )
        )
    } catch (e: SQLException) {
        Either.Left(e)
    }
}
