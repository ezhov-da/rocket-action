package ru.ezhov.rocket.action.application.chainaction.scheduler.application

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.InvalidCronExpressionException

internal class CronExpressionValidatorTest {
    @Test
    fun `should accept valid cron expressions`() {
        assertThatCode { CronExpressionValidator.validate("0 0/5 * * * ?") }
            .doesNotThrowAnyException()
        assertThatCode { CronExpressionValidator.validate("0 15 10 ? * MON-FRI") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `should report parser reason for invalid cron expression`() {
        val exception = assertThrows<InvalidCronExpressionException> {
            CronExpressionValidator.validate("not a cron expression")
        }

        assertThat(exception.cron).isEqualTo("not a cron expression")
        assertThat(exception.reason).isNotBlank()
    }
}
