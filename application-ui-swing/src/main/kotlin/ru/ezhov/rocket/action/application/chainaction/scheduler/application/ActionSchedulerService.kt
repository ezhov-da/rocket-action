package ru.ezhov.rocket.action.application.chainaction.scheduler.application

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.AtomicActionService
import ru.ezhov.rocket.action.application.chainaction.application.ChainActionService
import ru.ezhov.rocket.action.application.chainaction.domain.event.AtomicActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.domain.event.ChainActionDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.ActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.CreateOrUpdateActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.UpdateStatusActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.UpdateStatusInfoActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerLogRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.ActionSchedulerRepository
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerStatusUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionScheduler
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.model.ActionSchedulerStatus
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.time.LocalDateTime

@Service
class ActionSchedulerService(
    private val chainActionService: ChainActionService,
    private val atomicActionService: AtomicActionService,
    private val actionSchedulerRepository: ActionSchedulerRepository,
    private val actionSchedulerLogRepository: ActionSchedulerLogRepository,
) : InitializingBean {

    init {
        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is AtomicActionDeletedDomainEvent -> delete(event.id)
                    is ChainActionDeletedDomainEvent -> delete(event.id)
                    else -> {}
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(
                    AtomicActionDeletedDomainEvent::class.java,
                    ChainActionDeletedDomainEvent::class.java,
                )
        })
    }

    fun all(): List<ActionSchedulerDto> {
        val schedulers = actionSchedulerRepository.all()
        val actions = getActions()

        return schedulers.schedulers.mapNotNull { sch ->
            actions[sch.actionId]?.let {
                ActionSchedulerDto(
                    action = it,
                    actionScheduler = sch,
                    logFile = actionSchedulerLogRepository.get(sch.actionId)
                )
            }
        }
    }

    fun exists(actionId: String): Boolean =
        actionSchedulerRepository.all().schedulers.any { it.actionId == actionId }

    fun scheduler(actionId: String): ActionSchedulerDto? =
        actionSchedulerRepository
            .all()
            .schedulers
            .firstOrNull { it.actionId == actionId }
            ?.let { sch ->
                getActions().let { actions ->
                    actions[sch.actionId]?.let {
                        ActionSchedulerDto(
                            action = it,
                            actionScheduler = sch,
                            logFile = actionSchedulerLogRepository.get(sch.actionId)
                        )
                    }
                }
            }

    private fun getActions() =
        (chainActionService.chains() + atomicActionService.atomics())
            .associateBy { it.id() }

    fun createOrUpdate(action: CreateOrUpdateActionScheduler) {
        val scheduler = actionSchedulerRepository.all().schedulers.firstOrNull { it.actionId == action.actionId }

        actionSchedulerRepository.save(
            ActionScheduler(
                action.actionId,
                action.cron,
                dateOfTheLastLaunch = null,
                schedulerStatus = ActionSchedulerStatus.NOT_LAUNCHED
            )
        )

        DomainEventFactory.publisher.publish(
            listOf(
                when (scheduler) {
                    null -> ActionSchedulerCreatedDomainEvent(action.actionId)
                    else -> ActionSchedulerUpdatedDomainEvent(action.actionId)
                }
            )
        )
    }

    fun updateStatus(update: UpdateStatusActionSchedulerDto) {
        val now = LocalDateTime.now()
        actionSchedulerRepository
            .all()
            .schedulers
            .firstOrNull { it.actionId == update.actionId }
            ?.let { scheduler ->
                actionSchedulerRepository.save(
                    scheduler.apply {
                        this.dateOfTheLastLaunch = now
                        this.schedulerStatus = update.status.status
                    }
                )
            }

        when (update.status) {
            is UpdateStatusInfoActionSchedulerDto.Error ->
                actionSchedulerLogRepository.save(
                    update.actionId,
                    now,
                    update.status.status,
                    result = null,
                    ex = update.status.ex

                )

            is UpdateStatusInfoActionSchedulerDto.Success ->
                actionSchedulerLogRepository.save(
                    update.actionId,
                    now,
                    update.status.status,
                    result = update.status.result,
                    ex = null

                )
        }

        DomainEventFactory.publisher.publish(
            listOf(ActionSchedulerStatusUpdatedDomainEvent(update.actionId))
        )
    }

    fun delete(actionId: String) {
        actionSchedulerRepository.delete(actionId)
        actionSchedulerLogRepository.delete(actionId)
        DomainEventFactory.publisher.publish(listOf(ActionSchedulerDeletedDomainEvent(actionId)))
    }

    /**
     * Used only for access outside the Spring context.
     * Important! Can be null if called before the context is initialized.
     */
    companion object {
        var INSTANCE: ActionSchedulerService? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = this
    }
}
