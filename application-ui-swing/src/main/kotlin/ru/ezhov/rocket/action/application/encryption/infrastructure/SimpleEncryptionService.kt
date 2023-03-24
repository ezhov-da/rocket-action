package ru.ezhov.rocket.action.application.encryption.infrastructure

import ru.ezhov.rocket.action.application.encryption.domain.EncryptionService
import ru.ezhov.rocket.action.application.encryption.domain.model.Algorithm
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @param algorithmName https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
 */
class SimpleEncryptionService(
    private val algorithmName: String
) : EncryptionService {

    override fun algorithm(): Algorithm = Algorithm.BLOWFISH

    override fun encrypt(source: String, key: String): String {
        val sKeySpec = SecretKeySpec(key.toByteArray(), algorithmName)
        val cipher = Cipher.getInstance(algorithmName)
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec)
        val encrypted = cipher.doFinal(source.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    override fun decrypt(source: String, key: String): String {
        val decodeFromBase64 = Base64.getDecoder().decode(source)
        val sKeySpec = SecretKeySpec(key.toByteArray(), algorithmName)
        val cipher = Cipher.getInstance(algorithmName)
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec)
        val decrypted = cipher.doFinal(decodeFromBase64)
        return String(decrypted, Charsets.UTF_8)
    }
}
