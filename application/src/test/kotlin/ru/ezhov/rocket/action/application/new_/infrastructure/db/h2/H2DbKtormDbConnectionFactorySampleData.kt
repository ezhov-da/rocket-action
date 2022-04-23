package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbCredentialsFactory

object H2DbKtormDbConnectionFactorySampleData {
    fun default(factory: DbCredentialsFactory = TestH2DbCredentialsFactory()) =
        H2DbKtormDbConnectionFactory(factory)
}
