package ru.ezhov.rocket.action.application.chainaction.startup.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntryResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime

internal class FileStartupActionLogRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should return null before the first run`() {
        assertThat(repository().get()).isNull()
    }

    @Test
    fun `should append the trigger source and per-entry outcomes`() {
        val repository = repository()
        repository.save(result(StartupRunTrigger.STARTUP, StartupRunStatus.SUCCESS))
        repository.save(result(StartupRunTrigger.MANUAL, StartupRunStatus.ERROR))

        val log = repository.get()
        assertThat(log).isNotNull
        val text = log!!.readText()
        assertThat(text).contains("STARTUP")
        assertThat(text).contains("MANUAL")
        assertThat(text).contains("[SUCCESS]")
        assertThat(text).contains("[ERROR]")
        assertThat(text).contains("boom")
    }

    private fun repository(file: File = File(tempDir.toFile(), "log.txt")) =
        FileStartupActionLogRepository(file)

    private fun result(trigger: StartupRunTrigger, status: StartupRunStatus) = StartupRunResult(
        trigger = trigger,
        startedAt = LocalDateTime.now(),
        finishedAt = LocalDateTime.now(),
        entries = listOf(
            StartupActionEntryResult(
                entry = StartupActionEntry(id = "entry", actionId = "action"),
                status = status,
                result = if (status == StartupRunStatus.SUCCESS) "ok" else null,
                error = if (status == StartupRunStatus.ERROR) "boom" else null,
            )
        ),
    )
}
