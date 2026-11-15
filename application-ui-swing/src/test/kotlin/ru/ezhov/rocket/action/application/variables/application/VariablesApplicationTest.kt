package ru.ezhov.rocket.action.application.variables.application

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.ApplicationConfigurations
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManager
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.domain.VariableRepository
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables
import java.io.File

internal class VariablesApplicationTest {
    private val variablesFile = File.createTempFile("variables", ".json").apply { deleteOnExit() }
    private val configurationFile = File.createTempFile("configurations", ".json").apply { deleteOnExit() }
    private val missingKeePass = File.createTempFile("missing-keepass", ".kdbx").apply {
        delete()
        deleteOnExit()
    }

    @Test
    fun `should read the repository only once while the sources are unchanged`() {
        val variableRepository = mockk<VariableRepository>()
        every { variableRepository.all() } returns Variables(variables = listOf(applicationVariable()))

        val application = application(
            variableRepository = variableRepository,
            configurations = configurations(),
        )

        val first = application.all()
        val second = application.all()

        assertThat(first.variables).hasSize(1)
        assertThat(second).isSameAs(first)
        verify(exactly = 1) { variableRepository.all() }
    }

    @Test
    fun `should re-read the repository after explicit invalidation`() {
        val variableRepository = mockk<VariableRepository>()
        every { variableRepository.all() } returns Variables(variables = listOf(applicationVariable()))

        val application = application(
            variableRepository = variableRepository,
            configurations = configurations(),
        )

        application.all()
        application.invalidate()
        application.all()

        verify(exactly = 2) { variableRepository.all() }
    }

    @Test
    fun `should ignore a broken password store and keep the regular variables`() {
        val variableRepository = mockk<VariableRepository>()
        every { variableRepository.all() } returns Variables(variables = listOf(applicationVariable()))

        val application = application(
            variableRepository = variableRepository,
            configurations = configurations(
                variablesManagers = listOf(
                    VariablesManager.KeePassVariablesManager(
                        dbPath = missingKeePass.absolutePath,
                        passwordVariableName = "password",
                        variableRegExp = ".*",
                    )
                )
            ),
        )

        val result = application.all()

        assertThat(result.variables.map { it.name }).containsExactly("name")
    }

    @Test
    fun `should recover after the password store back-off is reset`() {
        val variableRepository = mockk<VariableRepository>()
        every { variableRepository.all() } returns Variables(variables = listOf(applicationVariable()))

        val application = application(
            variableRepository = variableRepository,
            configurations = configurations(
                variablesManagers = listOf(
                    VariablesManager.KeePassVariablesManager(
                        dbPath = missingKeePass.absolutePath,
                        passwordVariableName = "password",
                        variableRegExp = ".*",
                    )
                )
            ),
        )

        application.all()
        // invalidate resets the back-off, so the next call tries the store again and is still non-fatal
        application.invalidate()
        val afterReset = application.all()

        assertThat(afterReset.variables.map { it.name }).containsExactly("name")
    }

    private fun application(
        variableRepository: VariableRepository,
        configurations: ApplicationConfigurations,
    ): VariablesApplication {
        val configurationApplication = mockk<ConfigurationApplication>()
        every { configurationApplication.all() } returns configurations

        val properties = mockk<GeneralPropertiesRepository>()
        every { properties.asStringOrNull(UsedPropertiesName.VARIABLES_FILE_REPOSITORY_PATH) } returns
            variablesFile.absolutePath
        every { properties.asStringOrNull(UsedPropertiesName.APPLICATION_CONFIGURATION_FILE_REPOSITORY_PATH) } returns
            configurationFile.absolutePath

        return VariablesApplication(
            variableRepository = variableRepository,
            configurationApplication = configurationApplication,
            generalPropertiesRepository = properties,
        )
    }

    private fun configurations(
        variablesManagers: List<VariablesManager> = emptyList(),
    ) = ApplicationConfigurations(
        variablesKey = "key",
        variablesManagers = variablesManagers,
        numberButtonsOnChainActionSelectionPanel = 1,
        globalHotKeys = null,
    )

    private fun applicationVariable() = Variable(
        name = "name",
        value = "value",
        type = VariableType.APPLICATION,
    )
}
