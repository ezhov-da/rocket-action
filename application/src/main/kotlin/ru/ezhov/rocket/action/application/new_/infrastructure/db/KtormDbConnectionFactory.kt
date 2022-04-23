package ru.ezhov.rocket.action.application.new_.infrastructure.db

import arrow.core.Either
import arrow.core.handleErrorWith
import org.ktorm.database.Database

interface KtormDbConnectionFactory {
    fun database(): Either<Exception, Database>
}

fun <T> KtormDbConnectionFactory.database(
    exceptionMapper: (Exception) -> T
): Either<T, Database> =
    this.database()
        .handleErrorWith { ex ->
            Either.Left(exceptionMapper(ex))
        }
