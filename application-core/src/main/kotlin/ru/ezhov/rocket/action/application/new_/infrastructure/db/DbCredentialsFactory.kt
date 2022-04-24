package ru.ezhov.rocket.action.application.new_.infrastructure.db

interface DbCredentialsFactory {
    val url: String
    val user: String
    val password: String
}
