package ru.ezhov.rocket.action.core.infrastructure.db

interface DbCredentialsFactory {
    val url: String
    val user: String
    val password: String
}
