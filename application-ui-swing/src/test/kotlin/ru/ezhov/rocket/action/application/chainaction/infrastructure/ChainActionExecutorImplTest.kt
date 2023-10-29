package ru.ezhov.rocket.action.application.chainaction.infrastructure

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ChainActionExecutorProgress
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariableDto
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType

internal class ChainActionExecutorImplTest {
    @Test
    fun `should be success when execute chain`() {
        val atomicActionService = mockk<AtomicActionService> {
            every { atomicBy("11") } returns AtomicAction(
                id = "11",
                source = AtomicActionSource.TEXT,
                data = "_INPUT.toInteger()",
                engine = AtomicActionEngine.GROOVY,
                name = "String to Int",
                description = "String to Int",
            )

            every { atomicBy("22") } returns AtomicAction(
                id = "22",
                source = AtomicActionSource.TEXT,
                data = "_INPUT + 2",
                engine = AtomicActionEngine.KOTLIN,
                name = "Int plus Int",
                description = "Int plus Int",
            )
        }
        val chainActionExecutorImpl = ChainActionExecutorImpl(
            EngineFactory(),
            mockk {
                every { all() } returns VariablesDto(
                    key = "1213",
                    variables = mutableListOf(
                        VariableDto(
                            name = "text name",
                            value = "text value",
                            description = "test description",
                            type = VariableType.APPLICATION,
                        )
                    )
                )
            },
            atomicActionService,
        )

        chainActionExecutorImpl.execute(
            input = "111",
            chainAction = ChainAction(
                id = "123",
                name = "Test name",
                description = "test description",
                actions = listOf(
                    ActionOrder(
                        "000", "11"
                    ),
                    ActionOrder(
                        "111", "22"
                    )
                )
            ),
            object : ChainActionExecutorProgress {
                override fun complete(result: Any?) {
                    println("complete $result")
                }

                override fun success(orderId: String, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun failure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    println("failure $orderId")
                }
            }
        )
    }

    @Test
    fun `should be failure when execute chain`() {
        val atomicActionService = mockk<AtomicActionService> {
            every { atomicBy("11") } returns AtomicAction(
                id = "11",
                source = AtomicActionSource.TEXT,
                data = "_INPUT.toInteger()",
                engine = AtomicActionEngine.GROOVY,
                name = "String to Int",
                description = "String to Int",
            )
            every { atomicBy("22") } returns AtomicAction(
                id = "22",
                source = AtomicActionSource.TEXT,
                data = "_INPUT +. 2",
                engine = AtomicActionEngine.KOTLIN,
                name = "Int plus Int",
                description = "Int plus Int",
            )
        }

        val chainActionExecutorImpl = ChainActionExecutorImpl(
            EngineFactory(),
            mockk {
                every { all() } returns VariablesDto(
                    key = "1213",
                    variables = mutableListOf(
                        VariableDto(
                            name = "text name",
                            value = "text value",
                            description = "test description",
                            type = VariableType.APPLICATION,
                        )
                    )
                )
            },
            atomicActionService,
        )

        chainActionExecutorImpl.execute(
            input = "111",
            chainAction = ChainAction(
                id = "123",
                name = "Test name",
                description = "test description",
                actions = listOf(
                    ActionOrder(
                        "000", "11"
                    ),
                    ActionOrder(
                        "111", "22"
                    )
                )
            ),
            object : ChainActionExecutorProgress {
                override fun complete(result: Any?) {
                    println("complete $result")
                }

                override fun success(orderId: String, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun failure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    println("failure $orderId")
                }

            }
        )
    }
}
