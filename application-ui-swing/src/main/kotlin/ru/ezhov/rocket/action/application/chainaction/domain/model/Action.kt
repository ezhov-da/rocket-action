package ru.ezhov.rocket.action.application.chainaction.domain.model

interface Action {
    fun id(): String

    fun name(): String

    fun description(): String
}
