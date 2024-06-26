package ru.ezhov.rocket.action.application.chainaction.infrastructure

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.engine.application.EngineFactory
import ru.ezhov.rocket.action.application.variables.application.VariableDto
import ru.ezhov.rocket.action.application.variables.application.VariablesDto
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType

internal class ActionExecutorImplTest {
    @Test
    fun `should be success when execute chain`() {
        val atomicActionService = mockk<AtomicActionService> {
            every { atomicBy("11") } returns AtomicAction(
                id = "11",
                source = AtomicActionSource.TEXT,
                data = "_INPUT.toInteger()",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.GROOVY,
                name = "String to Int",
                description = "String to Int",
            )

            every { atomicBy("22") } returns AtomicAction(
                id = "22",
                source = AtomicActionSource.TEXT,
                data = "_INPUT + 2",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.KOTLIN,
                name = "Int plus Int",
                description = "Int plus Int",
            )
        }
        val chainActionExecutorImpl = ActionExecutorImpl(
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
            action = ChainAction(
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
            object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    println("complete $result")
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
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
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.GROOVY,
                name = "String to Int",
                description = "String to Int",
            )
            every { atomicBy("22") } returns AtomicAction(
                id = "22",
                source = AtomicActionSource.TEXT,
                data = "_INPUT +. 2",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.KOTLIN,
                name = "Int plus Int",
                description = "Int plus Int",
            )
        }

        val chainActionExecutorImpl = ActionExecutorImpl(
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
            action = ChainAction(
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
            object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    println("complete $result")
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    println("failure $orderId")
                }
            }
        )
    }

    @Test
    fun `should be success when execute atomic action`() {
        val chainActionExecutorImpl = ActionExecutorImpl(
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
            mockk(),
        )

        chainActionExecutorImpl.execute(
            input = "111",
            action = AtomicAction(
                id = "11",
                source = AtomicActionSource.TEXT,
                data = "_INPUT.toInteger()",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.GROOVY,
                name = "String to Int",
                description = "String to Int",
            ),
            object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    println("complete $result")
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    println("failure $orderId")
                }
            }
        )
    }

    @Test
    fun `should be failure when execute atomic action`() {
        val chainActionExecutorImpl = ActionExecutorImpl(
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
            mockk(),
        )

        chainActionExecutorImpl.execute(
            input = "111",
            action = AtomicAction(
                id = "22",
                source = AtomicActionSource.TEXT,
                data = "_INPUT +. 2",
                contractType = ContractType.IN_OUT,
                engine = AtomicActionEngine.KOTLIN,
                name = "Int plus Int",
                description = "Int plus Int",
            ),
            object : ProgressExecutingAction {
                override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                    println("complete $result")
                }

                override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                    println("success ${atomicAction.id}")
                }

                override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                    println("failure $orderId")
                }
            }
        )
    }
}
