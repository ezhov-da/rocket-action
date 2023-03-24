package ru.ezhov.rocket.action.application.encryption.application

import ru.ezhov.rocket.action.application.encryption.domain.EncryptionService
import ru.ezhov.rocket.action.application.encryption.domain.model.Algorithm
import ru.ezhov.rocket.action.application.encryption.infrastructure.SimpleEncryptionService

object EncryptionServiceFactory {
    fun get(algorithm: Algorithm): EncryptionService? =
        when (algorithm) {
            Algorithm.BLOWFISH -> SimpleEncryptionService("Blowfish")
        }
}
