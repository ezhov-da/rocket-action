package ru.ezhov.rocket.action.core.infrastructure.db

object ConnectionFactorySampleData {
    fun driverManager(factory: DbCredentialsFactory) = DriverManagerConnectionFactory(factory)
}
