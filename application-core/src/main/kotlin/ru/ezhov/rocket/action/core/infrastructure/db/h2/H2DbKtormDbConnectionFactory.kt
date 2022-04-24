package ru.ezhov.rocket.action.core.infrastructure.db.h2

import arrow.core.Either
import org.ktorm.database.Database
import ru.ezhov.rocket.action.core.infrastructure.db.DbCredentialsFactory
import ru.ezhov.rocket.action.core.infrastructure.db.KtormDbConnectionFactory

class H2DbKtormDbConnectionFactory(private val credentials: DbCredentialsFactory) : KtormDbConnectionFactory {
    override fun database(): Either<Exception, Database> = try {
        Either.Right(
            Database.connect(
                url = credentials.url,
                user = credentials.user,
                password = credentials.password
            )
        )
    } catch (e: Exception) {
        Either.Left(e)
    }
}
