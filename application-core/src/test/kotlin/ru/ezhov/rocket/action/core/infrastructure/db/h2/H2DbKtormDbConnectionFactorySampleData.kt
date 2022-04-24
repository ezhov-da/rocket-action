package ru.ezhov.rocket.action.core.infrastructure.db.h2

import ru.ezhov.rocket.action.core.infrastructure.db.DbCredentialsFactory

object H2DbKtormDbConnectionFactorySampleData {
    fun default(factory: DbCredentialsFactory = H2DbCredentialsFactorySampleData()) =
        H2DbKtormDbConnectionFactory(factory)
}
