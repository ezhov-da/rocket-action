package ru.ezhov.rocket.action.application.chainaction.interfaces.ui.renderer

import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.interfaces.ui.components.iconForContractTypes
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.plugin.markdown.Markdown
import javax.swing.Icon

/**
 * Everything a list cell needs to render one action row.
 *
 * The decoration is computed outside the event thread, so painting a cell never reads
 * repositories, queries services or parses Markdown.
 */
data class ActionRowDecoration(
    /**
     * Text ready for [javax.swing.JLabel.text]. Atomic action titles contain HTML,
     * chain action titles are the plain chain name.
     */
    val titleHtml: String,
    val schedulerStatus: ActionSchedulerStatus? = null,
    val usageCount: Int? = null,
    val chainIcon: Icon? = null,
)

/**
 * Immutable decoration lookup keyed by action id. A new snapshot is published on every refresh.
 */
class ActionRowDecorationSnapshot private constructor(
    private val decorations: Map<String, ActionRowDecoration>,
) {
    operator fun get(actionId: String): ActionRowDecoration? = decorations[actionId]

    val isEmpty: Boolean
        get() = decorations.isEmpty()

    companion object {
        val EMPTY = ActionRowDecorationSnapshot(emptyMap())

        fun of(decorations: Map<String, ActionRowDecoration>): ActionRowDecorationSnapshot =
            ActionRowDecorationSnapshot(decorations.toMap())
    }
}

/**
 * Builds a decoration snapshot from data that is already loaded.
 */
object ActionRowDecorationBuilder {
    fun build(
        chains: List<ChainAction>,
        atomics: List<AtomicAction>,
        schedulers: List<ActionScheduler>,
    ): ActionRowDecorationSnapshot {
        val atomicById = atomics.associateBy { it.id }
        val schedulerStatusByActionId = schedulers.associate { it.actionId to it.schedulerStatus }
        val usageByActionId = usageCounts(chains)

        val decorations = HashMap<String, ActionRowDecoration>(atomics.size + chains.size)

        atomics.forEach { action ->
            decorations[action.id] = ActionRowDecoration(
                titleHtml = atomicTitle(action, usageByActionId[action.id] ?: 0),
                schedulerStatus = schedulerStatusByActionId[action.id],
                usageCount = usageByActionId[action.id] ?: 0,
            )
        }

        chains.forEach { chain ->
            decorations[chain.id] = ActionRowDecoration(
                titleHtml = chain.name,
                schedulerStatus = schedulerStatusByActionId[chain.id],
                chainIcon = chainIcon(chain, atomicById),
            )
        }

        return ActionRowDecorationSnapshot.of(decorations)
    }

    private fun atomicTitle(action: AtomicAction, usageCount: Int): String {
        val alias = action.alias?.let { ". _${it}_" }.orEmpty()
        val usage = ". **$usageCount**"

        return "<html>" + Markdown.textMarkdownToHtml("${action.name}$usage$alias")
    }

    private fun usageCounts(chains: List<ChainAction>): Map<String, Int> {
        val counts = HashMap<String, Int>()
        chains.forEach { chain ->
            chain.actions
                .map { it.actionId }
                .distinct()
                .forEach { actionId -> counts[actionId] = (counts[actionId] ?: 0) + 1 }
        }

        return counts
    }

    /**
     * Same icon the rendering used to compute through [AtomicActionService]; now resolved
     * from the atomic actions that were loaded for the refresh.
     */
    private fun chainIcon(chain: ChainAction, atomicById: Map<String, AtomicAction>): Icon? {
        val first = chain.actions.firstOrNull()?.let { atomicById[it.actionId] }
        val last = chain.actions.lastOrNull()?.let { atomicById[it.actionId] }

        return iconForContractTypes(
            first = first?.contractType,
            second = last?.contractType,
        )
    }
}
