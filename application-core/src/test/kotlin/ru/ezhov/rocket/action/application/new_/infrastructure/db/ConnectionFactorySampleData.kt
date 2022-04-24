package ru.ezhov.rocket.action.application.new_.infrastructure.db

object ConnectionFactorySampleData {
    fun driverManager(factory: DbCredentialsFactory) = DriverManagerConnectionFactory(factory)
}
