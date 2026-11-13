package ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure

import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.ezhov.rocket.action.application.TestUtilsFactory
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulers
import ru.ezhov.rocket.action.application.chainaction.scheduler.infrastructure.model.JsonActionScheduler
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import java.io.File

class JsonFileActionSchedulerRepositoryTest {
    @TempDir
    lateinit var tempDir: File

    private fun repository(
        mapper: com.fasterxml.jackson.databind.ObjectMapper = TestUtilsFactory.objectMapper,
        folder: File = tempDir,
    ) = JsonFileActionSchedulerRepository(
        generalPropertiesRepository = mockk<GeneralPropertiesRepository>(),
        objectMapper = mapper,
        folderPathOverride = folder.absolutePath,
    )

    @Test
    fun `repeated reads parse the folder once`() {
        val mapper = spyk(TestUtilsFactory.objectMapper)
        val repository = repository(mapper)
        repository.save(scheduler("1", ActionSchedulerStatus.NOT_LAUNCHED))

        repository.all()
        repository.all()
        repository.byActionId("1")

        verify(exactly = 1) { mapper.readValue(any<File>(), JsonActionScheduler::class.java) }
    }

    @Test
    fun `save refreshes the snapshot and the index`() {
        val repository = repository()
        repository.save(scheduler("1", ActionSchedulerStatus.NOT_LAUNCHED))
        repository.all()

        repository.save(scheduler("1", ActionSchedulerStatus.SUCCESS))

        assertThat(repository.all().schedulers.single().schedulerStatus)
            .isEqualTo(ActionSchedulerStatus.SUCCESS)
        assertThat(repository.byActionId("1")?.schedulerStatus)
            .isEqualTo(ActionSchedulerStatus.SUCCESS)
    }

    @Test
    fun `delete refreshes the snapshot and the index`() {
        val repository = repository()
        repository.save(ActionSchedulers(listOf(scheduler("1", ActionSchedulerStatus.SUCCESS))))
        repository.all()

        repository.delete("1")

        assertThat(repository.all().schedulers).isEmpty()
        assertThat(repository.byActionId("1")).isNull()
    }

    @Test
    fun `reading does not create the folder`() {
        val missingFolder = File(tempDir, "missing")
        val repository = repository(folder = missingFolder)

        assertThat(repository.all().schedulers).isEmpty()

        assertThat(missingFolder).doesNotExist()
    }

    private fun scheduler(actionId: String, status: ActionSchedulerStatus) =
        ActionScheduler(
            actionId = actionId,
            cron = null,
            dateOfTheLastLaunch = null,
            schedulerStatus = status,
        )
}
