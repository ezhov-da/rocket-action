package ru.ezhov.rocket.action.application.search.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class SearchTextTransformerTest {
    @ParameterizedTest
    @MethodSource("args")
    fun `should be success when transformed text`(input: String, result: List<String>) {
        val service = SearchTextTransformer()
        assertThat(service.transformedText(input)).isEqualTo(result)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of("еуые", listOf("еуые", "test")),
            Arguments.of("IDEA", listOf("idea", "швуф")),
            Arguments.of("швуф", listOf("швуф", "idea")),
            Arguments.of("проверка", listOf("проверка", "ghjdthrf")),
            Arguments.of("ghjdthrf", listOf("ghjdthrf", "проверка")),
        )
    }
}
