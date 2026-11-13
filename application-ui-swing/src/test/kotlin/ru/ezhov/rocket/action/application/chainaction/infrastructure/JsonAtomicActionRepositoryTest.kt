package ru.ezhov.rocket.action.application.chainaction.infrastructure

import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.infrastructure.dto.AtomicActionsDto
import java.io.File

class JsonAtomicActionRepositoryTest {
    @TempDir
    lateinit var tempDir: File

    private fun repository(mapper: com.fasterxml.jackson.databind.ObjectMapper = TestUtilsFactory.objectMapper) =
        JsonAtomicActionRepository(mapper, File(tempDir, "atomic-actions.json"))

    @Test
    fun `two consecutive reads parse the file once`() {
        val mapper = spyk(TestUtilsFactory.objectMapper)
        val repository = repository(mapper)
        repository.save(action("1", "First"))

        repository.all()
        repository.all()

        verify(exactly = 1) { mapper.readValue(any<File>(), AtomicActionsDto::class.java) }
    }

    @Test
    fun `save refreshes the cache`() {
        val repository = repository()
        repository.save(action("1", "First"))

        assertThat(repository.all().map { it.name }).containsExactly("First")

        repository.save(action("2", "Second"))

        assertThat(repository.all().map { it.name }).containsExactly("First", "Second")
    }

    @Test
    fun `delete refreshes the cache`() {
        val repository = repository()
        repository.save(action("1", "First"))
        repository.save(action("2", "Second"))

        repository.delete("1")

        assertThat(repository.all().map { it.id }).containsExactly("2")
    }

    @Test
    fun `reads are served from the cache until a write happens`() {
        val repository = repository()
        repository.save(action("1", "First"))
        repository.all()

        // Rewrite the file outside the repository; the cached value is still returned.
        TestUtilsFactory.objectMapper.writerWithDefaultPrettyPrinter().writeValue(
            File(tempDir, "atomic-actions.json"),
            AtomicActionsDto(changedDate = java.time.LocalDateTime.now(), atomicActions = emptyList()),
        )

        assertThat(repository.all().map { it.id }).containsExactly("1")

        repository.save(action("2", "Second"))

        assertThat(repository.all().map { it.id }).containsExactly("1", "2")
    }

    private fun action(id: String, name: String) =
        AtomicAction(
            id = id,
            name = name,
            description = name,
            contractType = ContractType.IN_OUT,
            engine = AtomicActionEngine.GROOVY,
            source = AtomicActionSource.TEXT,
            data = name,
        )
}
