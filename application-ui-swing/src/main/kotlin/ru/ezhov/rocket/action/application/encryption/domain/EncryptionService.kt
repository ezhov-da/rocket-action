package ru.ezhov.rocket.action.application.encryption.domain

import ru.ezhov.rocket.action.application.encryption.domain.model.Algorithm

interface EncryptionService {
    fun algorithm(): Algorithm

    fun encrypt(source: String, key: String): String

    fun decrypt(source: String, key: String): String
}
