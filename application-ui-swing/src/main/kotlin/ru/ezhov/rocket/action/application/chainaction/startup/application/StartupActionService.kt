package ru.ezhov.rocket.action.application.chainaction.startup.application

import mu.KotlinLogging
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.ChainAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.InputValueContractType
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRepository
import ru.ezhov.rocket.action.application.chainaction.startup.domain.event.StartupActionsUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActionEntry
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupActions
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Reads and writes the startup list and answers questions about the actions it references.
 *
 * Deletion cleanup mirrors the scheduler: when an AtomicAction or a ChainAction is deleted,
 * its startup entries are removed as well.
 */
@Service
class StartupActionService(
    private val startupActionsRepository: StartupActionsRepository,
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
) : InitializingBean {

    init {
        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is AtomicActionDeletedDomainEvent -> deleteEntriesFor(event.id)
                    is ChainActionDeletedDomainEvent -> deleteEntriesFor(event.id)
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(
                    AtomicActionDeletedDomainEvent::class.java,
                    ChainActionDeletedDomainEvent::class.java,
                )
        })
    }

    fun startupActions(): StartupActions = startupActionsRepository.all()

    fun save(startupActions: StartupActions) {
        startupActionsRepository.save(startupActions)
        DomainEventFactory.publisher.publish(listOf(StartupActionsUpdatedDomainEvent()))
    }

    fun actions(): List<Action> = chainActionService.chains() + atomicActionService.atomics()

    fun actionById(actionId: String): Action? =
        chainActionService.byId(actionId) ?: atomicActionService.atomicBy(actionId)

    fun actionName(actionId: String): String? = actionById(actionId)?.name()

    /**
     * An action accepts the external input when its input contract is `IN`.
     * For a chain the contract of the first AtomicAction is used, because the executor
     * passes the external input only to the first step.
     */
    fun acceptsInput(action: Action): Boolean =
        when (action) {
            is AtomicAction -> action.contractType.inputValue == InputValueContractType.IN
            is ChainAction -> action.actions
                .firstOrNull()
                ?.let { atomicActionService.atomicBy(it.actionId) }
                ?.let { it.contractType.inputValue == InputValueContractType.IN }
                ?: false
            else -> false
        }

    fun acceptsInput(actionId: String): Boolean =
        actionById(actionId)?.let { acceptsInput(it) } ?: false

    fun isResolved(entry: StartupActionEntry): Boolean = actionById(entry.actionId) != null

    fun add(startupActions: StartupActions, actionId: String): StartupActions =
        startupActions.copy(
            startupActions = startupActions.startupActions + StartupActionEntry(
                id = UUID.randomUUID().toString(),
                actionId = actionId,
            )
        )

    fun remove(startupActions: StartupActions, entryId: String): StartupActions =
        startupActions.copy(
            startupActions = startupActions.startupActions.filterNot { it.id == entryId }
        )

    fun moveUp(startupActions: StartupActions, entryId: String): StartupActions =
        move(startupActions, entryId, -1)

    fun moveDown(startupActions: StartupActions, entryId: String): StartupActions =
        move(startupActions, entryId, 1)

    fun setEnabled(startupActions: StartupActions, entryId: String, enabled: Boolean): StartupActions =
        updateEntry(startupActions, entryId) { it.copy(enabled = enabled) }

    fun setInput(startupActions: StartupActions, entryId: String, input: String?): StartupActions =
        updateEntry(startupActions, entryId) { it.copy(input = input?.takeIf { value -> value.isNotEmpty() }) }

    fun setStopOnError(startupActions: StartupActions, stopOnError: Boolean): StartupActions =
        startupActions.copy(stopOnError = stopOnError)

    /**
     * Removes every entry that references [actionId]. Used when the action is deleted.
     */
    fun deleteEntriesFor(actionId: String) {
        val current = startupActionsRepository.all()
        val remaining = current.startupActions.filterNot { it.actionId == actionId }
        if (remaining.size == current.startupActions.size) {
            return
        }

        startupActionsRepository.save(current.copy(startupActions = remaining))
        logger.info { "Removed startup entries of the deleted action '$actionId'" }
        DomainEventFactory.publisher.publish(listOf(StartupActionsUpdatedDomainEvent()))
    }

    private fun move(
        startupActions: StartupActions,
        entryId: String,
        offset: Int,
    ): StartupActions {
        val entries = startupActions.startupActions
        val index = entries.indexOfFirst { it.id == entryId }
        val target = index + offset
        if (index < 0 || target < 0 || target >= entries.size) {
            return startupActions
        }

        val reordered = entries.toMutableList()
        val entry = reordered.removeAt(index)
        reordered.add(target, entry)

        return startupActions.copy(startupActions = reordered)
    }

    private fun updateEntry(
        startupActions: StartupActions,
        entryId: String,
        update: (StartupActionEntry) -> StartupActionEntry,
    ): StartupActions =
        startupActions.copy(
            startupActions = startupActions.startupActions.map {
                if (it.id == entryId) update(it) else it
            }
        )

    override fun afterPropertiesSet() {
        INSTANCE = this
    }

    /**
     * Used only for access outside the Spring context.
     * Important! Can be null if called before the context is initialized.
     */
    companion object {
        var INSTANCE: StartupActionService? = null
    }
}
