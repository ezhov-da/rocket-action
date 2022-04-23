package ru.ezhov.rocket.action.application.new_.infrastructure.db

import arrow.core.Either
import org.ktorm.database.Database

interface KtormDbConnectionFactory {
    fun database(): Either<Exception, Database>
}
