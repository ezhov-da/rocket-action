package ru.ezhov.rocket.action.core.infrastructure.db

import arrow.core.Either
import java.sql.Connection
import java.sql.SQLException

interface DbConnectionFactory {
    fun connection(): Either<SQLException, Connection>
}
