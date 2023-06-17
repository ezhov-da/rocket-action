package ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource


internal class RawTextValidatorTest {
    @ParameterizedTest
    @MethodSource("args")
    fun `test`(rules: String, source: String, errors: List<String>) {
        assertThat(RawTextValidator(rules).validate(source)).isEqualTo(errors)
    }

    companion object {
        @JvmStatic
        fun args() = listOf(
            Arguments.of(
                """
                    MIN_LENGTH 3
                    MAX_LENGTH 5
                """.trimIndent(),
                "12345",
                emptyList<String>()
            ),
            Arguments.of(
                """
                                         MIN_LENGTH 3
                    MAX_LENGTH 5
                """.trimIndent(),
                "12345",
                emptyList<String>()
            ),
            Arguments.of(
                """
                    MIN_LENGTH 3
                    MAX_LENGTH 5
                """.trimIndent(),
                "1",
                listOf("The minimum length is '3' characters. Now '1'")
            ),
            Arguments.of(
                """
                    MIN_LENGTH 3
                    MAX_LENGTH 5
                """.trimIndent(),
                "11111111111",
                listOf("Максимальная длина '5' символов. Сейчас '11'")
            ),
            Arguments.of(
                """
                    asgasdfgsagdfg
                """.trimIndent(),
                "11111111111",
                emptyList<String>()
            ),
            Arguments.of(
                """
                    MIN_LENGTH qqqqqqqqqq
                    MAX_LENGTH wwwwwwwwww
                """.trimIndent(),
                "11111111111",
                emptyList<String>()
            )
        )
    }
}
