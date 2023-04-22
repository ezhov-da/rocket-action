package ru.ezhov.rocket.action.application.encryption.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class SimpleEncryptionServiceTest {
    @ParameterizedTest
    @MethodSource("args")
    fun `should correct encrypt and decrypt`(originText: String, encryptedTextForEquals: String) {
        val key = "234hw4b45437b44684568"
        val service = SimpleEncryptionService("Blowfish")

        val encryptedText = service.encrypt(originText, key)
        assertThat(encryptedText).isEqualTo(encryptedTextForEquals)

        val decryptedText = service.decrypt(encryptedText, key)
        assertThat(decryptedText).isEqualTo(originText)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of(
                "Hello world!",
                "g7mTNuCEqoHfQV+9WGDhsQ==",
            ),
            Arguments.of(
                "Привет мир!!",
                "Cy59pwF6WWgjdJDyxZR1XLrhpAd9L1eV",
            ),
            Arguments.of(
                "*%*)%(?;(?№(?№?(",
                "IUGfyEVuOeANFQHU0PLVT5bNhFOOXJbO",
            ),
            Arguments.of(
                "sadgfsagda ыфафыпафпы savasdvaswv 434624364357435 )*;*:№(?№(?№",
                "ezTcU7CD8hwxwzS0OxQxdZB6Ywo0veMlrM83fZOKR2y9y5GLR2VUhCK1o6eyxzZgpR1IBtldjTUhfu+rM2TYrGubioW3km/6MpIhg/Dzy2o=",
            ),
        )
    }
}
