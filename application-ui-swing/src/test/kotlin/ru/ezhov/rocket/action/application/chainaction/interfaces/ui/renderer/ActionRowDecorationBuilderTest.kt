package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.chainaction.domain.model.ActionOrder
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionEngine
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicActionSource
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ContractType
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus

class ActionRowDecorationBuilderTest {
    private val first = atomic("first", "First action", ContractType.IN_OUT)
    private val second = atomic("second", "Second action", ContractType.IN_UNIT, alias = "second-alias")

    private val chain = ChainAction(
        id = "chain",
        name = "Chain action",
        description = "Chain description",
        actions = listOf(ActionOrder("o1", first.id), ActionOrder("o2", second.id)),
    )
    private val otherChain = ChainAction(
        id = "other-chain",
        name = "Other chain",
        description = "Other description",
        actions = listOf(ActionOrder("o3", first.id)),
    )

    @Test
    fun `usage counts and scheduler statuses match the source data`() {
        val snapshot = ActionRowDecorationBuilder.build(
            chains = listOf(chain, otherChain),
            atomics = listOf(first, second),
            schedulers = listOf(
                scheduler(first.id, ActionSchedulerStatus.SUCCESS),
                scheduler(chain.id, ActionSchedulerStatus.ERROR),
            ),
        )

        val firstDecoration = snapshot[first.id]!!
        assertThat(firstDecoration.usageCount).isEqualTo(2)
        assertThat(firstDecoration.schedulerStatus).isEqualTo(ActionSchedulerStatus.SUCCESS)
        assertThat(firstDecoration.titleHtml).contains("First action").contains("<strong>2</strong>")

        val secondDecoration = snapshot[second.id]!!
        assertThat(secondDecoration.usageCount).isEqualTo(1)
        assertThat(secondDecoration.schedulerStatus).isNull()
        assertThat(secondDecoration.titleHtml).contains("second-alias")

        val chainDecoration = snapshot[chain.id]!!
        assertThat(chainDecoration.schedulerStatus).isEqualTo(ActionSchedulerStatus.ERROR)
        assertThat(chainDecoration.usageCount).isNull()
        assertThat(chainDecoration.chainIcon).isNotNull
    }

    @Test
    fun `an action without chains or a scheduler gets empty decoration values`() {
        val decoration = ActionRowDecorationBuilder
            .build(chains = emptyList(), atomics = listOf(second), schedulers = emptyList())[second.id]!!

        assertThat(decoration.usageCount).isZero()
        assertThat(decoration.schedulerStatus).isNull()
    }

    @Test
    fun `rebuilding from retained data reflects an updated action name`() {
        val renamed = first.copy(name = "Renamed action")
        val decoration = ActionRowDecorationBuilder
            .build(chains = listOf(chain), atomics = listOf(renamed, second), schedulers = emptyList())[first.id]!!

        assertThat(decoration.titleHtml).contains("Renamed action")
    }

    @Test
    fun `rebuilding from an updated scheduler list changes only the decoration`() {
        val before = ActionRowDecorationBuilder
            .build(chains = listOf(chain), atomics = listOf(first), schedulers = emptyList())[first.id]!!
        val after = ActionRowDecorationBuilder.build(
            chains = listOf(chain),
            atomics = listOf(first),
            schedulers = listOf(scheduler(first.id, ActionSchedulerStatus.ERROR)),
        )[first.id]!!

        assertThat(before.schedulerStatus).isNull()
        assertThat(after.schedulerStatus).isEqualTo(ActionSchedulerStatus.ERROR)
    }

    private fun atomic(
        id: String,
        name: String,
        contractType: ContractType,
        alias: String? = null,
    ) = AtomicAction(
        id = id,
        name = name,
        description = name,
        contractType = contractType,
        engine = AtomicActionEngine.GROOVY,
        source = AtomicActionSource.TEXT,
        data = name,
        alias = alias,
    )

    private fun scheduler(actionId: String, status: ActionSchedulerStatus) =
        ActionScheduler(
            actionId = actionId,
            cron = null,
            dateOfTheLastLaunch = null,
            schedulerStatus = status,
        )
}
