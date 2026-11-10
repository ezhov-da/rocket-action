package ru.ezhov.rocket.action.plugin.script.dynamic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FieldNamesServiceTest {
    @ParameterizedTest
    @MethodSource("args")
    fun test(input: String, result: List<FieldName>) {
        assertThat(
            FieldNamesService().get(input, ":")
        )
            .isEqualTo(result)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of(
                """
                    11111
                    22222:333
                    44444:
                    5555:666:777
                """.trimIndent(),
                listOf(
                    FieldName(name = "11111", value = ""),
                    FieldName(name = "22222", value = "333"),
                    FieldName(name = "44444", value = ""),
                    FieldName(name = "5555", value = "666:777")
                )
            )
        )
    }
}
