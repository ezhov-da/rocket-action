package ru.ezhov.rocket.action.application.encryption.domain.model

enum class Algorithm {
    BLOWFISH;

    companion object {
        fun of(name: String): Algorithm? = Algorithm.values().firstOrNull { it.name == name }
    }
}
