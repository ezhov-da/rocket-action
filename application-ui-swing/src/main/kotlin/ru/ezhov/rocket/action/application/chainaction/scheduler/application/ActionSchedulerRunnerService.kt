package ru.ezhov.rocket.action.application.chainaction.scheduler.application

import mu.KotlinLogging
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.CronTrigger
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerBuilder.newTrigger
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.application.ActionExecutorService
import ru.ezhov.rocket.action.application.chainaction.domain.ProgressExecutingAction
import ru.ezhov.rocket.action.application.chainaction.domain.model.AtomicAction
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.UpdateStatusActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.application.model.UpdateStatusInfoActionSchedulerDto
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerCreatedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerDeletedDomainEvent
import ru.ezhov.rocket.action.application.chainaction.scheduler.domain.event.ActionSchedulerUpdatedDomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.scheduler.SchedulerSingleton

private val logger = KotlinLogging.logger { }
private const val JOB_GROUP_NAME = "actions"
private const val JOB_DATA_KEY_ACTION_ID = "actionId"
private const val JOB_DATA_KEY_ACTION_SCHEDULER_SERVICE = "actionSchedulerService"
private const val JOB_DATA_KEY_ACTION_EXECUTOR_SERVICE = "actionExecutorService"

@Service
class ActionSchedulerRunnerService(
    private val actionSchedulerService: ActionSchedulerService,
    private val schedulerSingleton: SchedulerSingleton,
    private val actionExecutorService: ActionExecutorService,
) : InitializingBean {
    override fun afterPropertiesSet() {
        startExistsSchedulers()

        DomainEventFactory.subscriberRegistrar.subscribe(object : DomainEventSubscriber {
            override fun handleEvent(event: DomainEvent) {
                when (event) {
                    is ActionSchedulerCreatedDomainEvent -> registerActionScheduler(event.actionId, schedulerSingleton.get())
                    is ActionSchedulerDeletedDomainEvent -> deleteJob(event.actionId)
                    is ActionSchedulerUpdatedDomainEvent -> update(event.actionId)
                }
            }

            override fun subscribedToEventType(): List<Class<*>> =
                listOf(
                    ActionSchedulerCreatedDomainEvent::class.java,
                    ActionSchedulerDeletedDomainEvent::class.java,
                    ActionSchedulerUpdatedDomainEvent::class.java,
                )
        })
    }

    private fun startExistsSchedulers() {
        val runners = actionSchedulerService.all().filter { it.actionScheduler.cron != null }
        val scheduler = schedulerSingleton.get()
        runners.forEach { sh ->
            registerActionScheduler(sh.action.id(), scheduler)
        }
    }

    private fun registerActionScheduler(
        actionId: String,
        scheduler: Scheduler
    ) {
        actionSchedulerService
            .all()
            .firstOrNull { it.actionScheduler.actionId == actionId }
            ?.let { asch ->
                val job: JobDetail = JobBuilder
                    .newJob(ActionExecuteJob::class.java)
                    .usingJobData(
                        JobDataMap(
                            mutableMapOf(
                                JOB_DATA_KEY_ACTION_ID to actionId,
                                JOB_DATA_KEY_ACTION_SCHEDULER_SERVICE to actionSchedulerService,
                                JOB_DATA_KEY_ACTION_EXECUTOR_SERVICE to actionExecutorService,
                            )
                        )
                    )
                    .withIdentity(actionId, JOB_GROUP_NAME)
                    .build()

                val trigger: CronTrigger = newTrigger()
                    .withIdentity(actionId, JOB_GROUP_NAME)
                    .withSchedule(cronSchedule(asch.actionScheduler.cron))
                    .build()

                scheduler.scheduleJob(job, trigger)

                logger.info { "Job with ID '$actionId' is registered" }
            }
    }

    private fun update(actionId: String) {
        actionSchedulerService.all().firstOrNull { it.actionScheduler.actionId == actionId }?.let { asch ->
            deleteJob(actionId)
            registerActionScheduler(asch.action.id(), schedulerSingleton.get())

            logger.info { "Job with ID '$actionId' is updated" }
        }
    }

    private fun deleteJob(actionId: String) {
        val scheduler = schedulerSingleton.get()
        scheduler.deleteJob(JobKey(actionId, JOB_GROUP_NAME))

        logger.info { "Job with ID '$actionId' is deleted" }
    }

    class ActionExecuteJob : Job {
        override fun execute(context: JobExecutionContext) {
            val actionId = context.jobDetail.jobDataMap.getString(JOB_DATA_KEY_ACTION_ID)
            val actionSchedulerService = context.jobDetail.jobDataMap[JOB_DATA_KEY_ACTION_SCHEDULER_SERVICE] as ActionSchedulerService
            val actionExecutorService = context.jobDetail.jobDataMap[JOB_DATA_KEY_ACTION_EXECUTOR_SERVICE] as ActionExecutorService
            actionSchedulerService.scheduler(actionId)?.let { actionSch ->
                actionExecutorService.actionExecutor.execute(
                    input = null,
                    action = actionSch.action,
                    progressExecutingAction = object : ProgressExecutingAction {
                        override fun onComplete(result: Any?, lastAtomicAction: AtomicAction) {
                            actionSchedulerService.updateStatus(
                                UpdateStatusActionSchedulerDto(
                                    actionId = actionId,
                                    status = UpdateStatusInfoActionSchedulerDto.Success(result.toString())
                                )
                            )
                        }

                        override fun onAtomicActionSuccess(orderId: String, result: Any?, atomicAction: AtomicAction) {
                            println(result)
                        }

                        override fun onAtomicActionFailure(orderId: String, atomicAction: AtomicAction?, ex: Exception) {
                            actionSchedulerService.updateStatus(
                                UpdateStatusActionSchedulerDto(
                                    actionId = actionId,
                                    status = UpdateStatusInfoActionSchedulerDto.Error(ex)
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}
