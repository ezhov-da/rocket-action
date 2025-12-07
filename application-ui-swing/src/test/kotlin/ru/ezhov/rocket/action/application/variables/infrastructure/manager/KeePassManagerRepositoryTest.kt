package ru.ezhov.rocket.action.application.variables.infrastructure.manager

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManager
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType

class KeePassManagerRepositoryTest {
    @Test
    fun `should be success when read kee pass`() {
        val repository = KeePassManagerRepository()
        val variables = repository
            .variables(
                password = "test",
                manager = mockk<VariablesManager.KeePassVariablesManager> {
                    every { dbPath } returns "../keepass/test.kdbx"
                    every { variableRegExp } returns "RA_([A-Z_]+)"
                }
            )

        assertThat(variables).hasSize(1)
        assertThat(variables[0].name).isEqualTo("TEST_SECRET")
        assertThat(variables[0].value).isEqualTo("ndNKZ7dRbFGwQDHjK3jO")
        assertThat(variables[0].type).isEqualTo(VariableType.KEE_PASS)
    }
}
