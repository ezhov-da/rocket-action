package ru.ezhov.rocket.action.application.chainaction.startup.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import java.io.File
import java.nio.file.Path

internal class JsonStartupActionsRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should save and read the ordered list`() {
        val repository = repository()
        val startupActions = StartupActions(
            stopOnError = true,
            startupActions = listOf(
                StartupActionEntry(id = "1", actionId = "first", input = "value"),
                StartupActionEntry(id = "2", actionId = "second", enabled = false),
            ),
        )

        repository.save(startupActions)

        val read = repository.all()
        assertThat(read).isEqualTo(startupActions)
    }

    @Test
    fun `should return an empty list when the file does not exist`() {
        val read = repository().all()

        assertThat(read).isEqualTo(StartupActions())
    }

    @Test
    fun `should return an empty list when the file is malformed`() {
        val file = File(tempDir.toFile(), "startup-actions.json")
        file.writeText("{ this is not json")

        val read = repository(file).all()

        assertThat(read).isEqualTo(StartupActions())
    }

    private fun repository(
        file: File = File(tempDir.toFile(), "startup-actions.json"),
    ) = JsonStartupActionsRepository(file, TestUtilsFactory.objectMapper)
}
