package ru.ezhov.rocket.action.application.chainaction.startup.application

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.ActionExecutor
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionLogRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunResult
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunStatus
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

internal class StartupActionRunnerTest {
    private val okAction = atomic("ok")
    private val secondOkAction = atomic("ok2")
    private val failAction = atomic("fail")
    private val noInputAction = atomic("noInput")

    private lateinit var repository: InMemoryStartupActionsRepository
    private lateinit var logRepository: RecordingLogRepository
    private lateinit var runner: StartupActionRunner

    private val executed = mutableListOf<String>()
    private val executedInputs = mutableListOf<Pair<String, String?>>()
    private var automaticExecutionEnabled = true
    private var blocking: CountDownLatch? = null
    private var entered: CountDownLatch? = null

    @BeforeEach
    fun setUp() {
        repository = InMemoryStartupActionsRepository()
        logRepository = RecordingLogRepository()
        executed.clear()
        executedInputs.clear()
        automaticExecutionEnabled = true

        val executor = mockk<ActionExecutor> {
            every { execute(any(), any(), any()) } answers {
                val input = firstArg<String?>()
                val action = secondArg<Action>()
                val progress = thirdArg<ProgressExecutingAction>()
                executed += action.id()
                executedInputs += action.id() to input
                entered?.countDown()
                blocking?.await(5, TimeUnit.SECONDS)

                when (action.id()) {
                    "fail" -> progress.onAtomicActionFailure(
                        orderId = "order",
                        atomicAction = action as AtomicAction,
                        ex = IllegalStateException("boom"),
                    )

                    else -> progress.onComplete(result = "result-${action.id()}", lastAtomicAction = action as AtomicAction)
                }
            }
        }

        val actionExecutorService = mockk<ActionExecutorService> {
            every { actionExecutor } returns executor
        }

        val actionService = mockk<StartupActionService> {
            every { actionById(any()) } answers {
                when (firstArg<String>()) {
                    "ok" -> okAction
                    "ok2" -> secondOkAction
                    "fail" -> failAction
                    "noInput" -> noInputAction
                    "unexpected" -> throw IllegalStateException("unexpected failure")
                    else -> null
                }
            }
            every { acceptsInput(any<Action>()) } answers { firstArg<Action>().id() != "noInput" }
        }

        val properties = mockk<GeneralPropertiesRepository> {
            every { asBoolean(UsedPropertiesName.STARTUP_ACTIONS_ENABLED, true) } answers {
                automaticExecutionEnabled
            }
        }

        runner = StartupActionRunner(
            startupActionsRepository = repository,
            startupActionService = actionService,
            startupActionLogRepository = logRepository,
            actionExecutorService = actionExecutorService,
            generalPropertiesRepository = properties,
        )
    }

    @Test
    fun `should execute enabled entries in order and record success`() {
        repository.value = StartupActions(
            startupActions = listOf(entry("ok"), entry("ok2"))
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)

        assertThat(executed).containsExactly("ok", "ok2")
        assertThat(result!!.entries.map { it.status })
            .containsExactly(StartupRunStatus.SUCCESS, StartupRunStatus.SUCCESS)
        assertThat(result.trigger).isEqualTo(StartupRunTrigger.STARTUP)
        assertThat(result.isFinished).isTrue
        assertThat(runner.state().running).isFalse
        assertThat(logRepository.saved).containsExactly(result)
    }

    @Test
    fun `should pass input only to actions that accept it`() {
        repository.value = StartupActions(
            startupActions = listOf(
                entry("ok", input = "first value"),
                entry("noInput", input = "ignored"),
            )
        )

        runner.runBlocking(StartupRunTrigger.STARTUP)

        assertThat(executedInputs).containsExactly(
            "ok" to "first value",
            "noInput" to null,
        )
    }

    @Test
    fun `should continue after a failure when stop on error is disabled`() {
        repository.value = StartupActions(
            stopOnError = false,
            startupActions = listOf(entry("fail"), entry("ok2")),
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)!!

        assertThat(executed).containsExactly("fail", "ok2")
        assertThat(result.entries.map { it.status })
            .containsExactly(StartupRunStatus.ERROR, StartupRunStatus.SUCCESS)
        assertThat(result.entries.first().error).contains("boom")
    }

    @Test
    fun `should stop after a failure when stop on error is enabled`() {
        repository.value = StartupActions(
            stopOnError = true,
            startupActions = listOf(entry("fail"), entry("ok2")),
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)!!

        assertThat(executed).containsExactly("fail")
        assertThat(result.entries.map { it.status })
            .containsExactly(StartupRunStatus.ERROR, StartupRunStatus.SKIPPED)
    }

    @Test
    fun `should skip a missing action without stopping the run`() {
        repository.value = StartupActions(
            stopOnError = true,
            startupActions = listOf(entry("missing"), entry("ok2")),
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)!!

        assertThat(executed).containsExactly("ok2")
        assertThat(result.entries.map { it.status })
            .containsExactly(StartupRunStatus.UNRESOLVED, StartupRunStatus.SUCCESS)
    }

    @Test
    fun `should skip disabled entries`() {
        repository.value = StartupActions(
            startupActions = listOf(entry("ok", enabled = false), entry("ok2")),
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)!!

        assertThat(executed).containsExactly("ok2")
        assertThat(result.entries.map { it.status })
            .containsExactly(StartupRunStatus.SKIPPED, StartupRunStatus.SUCCESS)
    }

    @Test
    fun `should publish the finish event when an unexpected error happens`() {
        repository.value = StartupActions(
            startupActions = listOf(entry("unexpected"), entry("ok2")),
        )

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)!!

        assertThat(result.entries.map { it.status })
            .containsExactly(StartupRunStatus.ERROR, StartupRunStatus.NOT_RUN)
        assertThat(runner.state().running).isFalse
        assertThat(runner.state().lastResult).isEqualTo(result)
    }

    @Test
    fun `should not run automatically when the kill switch is disabled`() {
        automaticExecutionEnabled = false
        repository.value = StartupActions(startupActions = listOf(entry("ok")))

        val result = runner.runBlocking(StartupRunTrigger.STARTUP)

        assertThat(result).isNull()
        assertThat(executed).isEmpty()
        assertThat(repository.value.startupActions).hasSize(1)
    }

    @Test
    fun `should still allow a manual run when the kill switch is disabled`() {
        automaticExecutionEnabled = false
        repository.value = StartupActions(startupActions = listOf(entry("ok")))

        val result = runner.runBlocking(StartupRunTrigger.MANUAL)

        assertThat(result).isNotNull
        assertThat(result!!.trigger).isEqualTo(StartupRunTrigger.MANUAL)
        assertThat(executed).containsExactly("ok")
    }

    @Test
    fun `should ignore a second run while the first one is active`() {
        repository.value = StartupActions(startupActions = listOf(entry("ok")))
        entered = CountDownLatch(1)
        blocking = CountDownLatch(1)

        var firstResult: StartupRunResult? = null
        val thread = Thread { firstResult = runner.runBlocking(StartupRunTrigger.STARTUP) }
        thread.start()

        assertThat(entered!!.await(5, TimeUnit.SECONDS)).isTrue
        val secondResult = runner.runBlocking(StartupRunTrigger.MANUAL)
        blocking!!.countDown()
        thread.join(5000)

        assertThat(secondResult).isNull()
        assertThat(firstResult).isNotNull
    }

    private fun entry(actionId: String, enabled: Boolean = true, input: String? = null) =
        StartupActionEntry(id = "entry-$actionId", actionId = actionId, input = input, enabled = enabled)

    private fun atomic(id: String) = AtomicAction(
        id = id,
        name = id,
        description = "",
        contractType = ContractType.IN_OUT,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = "",
    )

    private class InMemoryStartupActionsRepository : StartupActionsRepository {
        var value: StartupActions = StartupActions()

        override fun all(): StartupActions = value

        override fun save(startupActions: StartupActions) {
            value = startupActions
        }
    }

    private class RecordingLogRepository : StartupActionLogRepository {
        val saved = mutableListOf<StartupRunResult>()

        override fun get(): File? = null

        override fun save(result: StartupRunResult) {
            saved += result
        }
    }
}
