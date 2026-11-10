package ru.ezhov.rocket.action.application.chainaction.startup.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class StartupActionsModelTest {
    @Test
    fun `should keep the order of entries`() {
        val startupActions = StartupActions(
            startupActions = listOf(
                StartupActionEntry(id = "1", actionId = "a"),
                StartupActionEntry(id = "2", actionId = "b"),
                StartupActionEntry(id = "3", actionId = "c"),
            )
        )

        assertThat(startupActions.startupActions.map { it.actionId }).containsExactly("a", "b", "c")
    }

    @Test
    fun `should identify entries by id, not by action reference`() {
        val first = StartupActionEntry(id = "entry-1", actionId = "same-action")
        val second = StartupActionEntry(id = "entry-2", actionId = "same-action")

        assertThat(first).isNotEqualTo(second)
        assertThat(listOf(first, second).map { it.id }).containsExactly("entry-1", "entry-2")
    }

    @Test
    fun `should count result statuses`() {
        val success = StartupActionEntry(id = "1", actionId = "a")
        val error = StartupActionEntry(id = "2", actionId = "b")
        val skipped = StartupActionEntry(id = "3", actionId = "c")
        val unresolved = StartupActionEntry(id = "4", actionId = "d")

        val result = StartupRunResult(
            trigger = StartupRunTrigger.MANUAL,
            startedAt = LocalDateTime.now(),
            finishedAt = LocalDateTime.now(),
            entries = listOf(
                StartupActionEntryResult(success, StartupRunStatus.SUCCESS, result = "ok"),
                StartupActionEntryResult(error, StartupRunStatus.ERROR, error = "boom"),
                StartupActionEntryResult(skipped, StartupRunStatus.SKIPPED),
                StartupActionEntryResult(unresolved, StartupRunStatus.UNRESOLVED, error = "not found"),
            ),
        )

        assertThat(result.successCount).isEqualTo(1)
        assertThat(result.errorCount).isEqualTo(1)
        assertThat(result.skippedCount).isEqualTo(1)
        assertThat(result.unresolvedCount).isEqualTo(1)
        assertThat(result.isFinished).isTrue
    }

    @Test
    fun `should report an unfinished run and idle state`() {
        val result = StartupRunResult(trigger = StartupRunTrigger.STARTUP, startedAt = LocalDateTime.now())

        assertThat(result.isFinished).isFalse
        assertThat(StartupRunState.idle().running).isFalse
        assertThat(StartupRunState.idle(result).lastResult).isSameAs(result)
    }
}
