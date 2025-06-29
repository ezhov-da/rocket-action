package ru.ezhov.rocket.action.application.variables.infrastructure.importv

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class PlainTextImportVariablesServiceTest {
    @ParameterizedTest
    @MethodSource("args")
    fun `should be success when parse variables`(arg: String, size: Int) {
        assertThat(PlainTextImportVariablesService(arg).variables()).hasSize(size)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of("", 0),
            Arguments.of("123", 0),
            Arguments.of("123=123", 1),
            Arguments.of("123=123=1111", 1),
            Arguments.of("123=123=1111\n12", 1),
            Arguments.of(
                """
                    123=123
                    12=321-12
                """.trimIndent(),
                2
            ),
        )
    }
}
