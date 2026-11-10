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
import org.quartz.Trigger
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.TriggerKey
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
            try {
                registerActionScheduler(sh.action.id(), scheduler)
            } catch (e: Exception) {
                // A broken schedule must not prevent the application (and other schedules) from starting.
                logger.error(e) { "Can not register scheduler for action '${sh.action.id()}'" }
            }
        }
    }

    private fun registerActionScheduler(
        actionId: String,
        scheduler: Scheduler
    ) {
        val definition = buildJobDefinition(actionId)
        if (definition == null) {
            logger.info { "Job with ID '$actionId' is not registered: cron expression is empty" }
            return
        }

        scheduler.scheduleJob(definition.job, definition.trigger)

        logger.info { "Job with ID '$actionId' is registered" }
    }

    /**
     * Builds a job/trigger pair without touching the scheduler, so an invalid cron
     * expression cannot remove an already registered schedule.
     */
    private fun buildJobDefinition(actionId: String): JobDefinition? {
        val asch = actionSchedulerService
            .all()
            .firstOrNull { it.actionScheduler.actionId == actionId }
            ?: return null

        val cron = asch.actionScheduler.cron?.trim()
        if (cron.isNullOrEmpty()) {
            return null
        }

        CronExpressionValidator.validate(cron)

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
            .withSchedule(cronSchedule(cron))
            .build()

        return JobDefinition(job = job, trigger = trigger)
    }

    private fun update(actionId: String) {
        if (actionSchedulerService.all().none { it.actionScheduler.actionId == actionId }) {
            return
        }

        // Build (and validate) the new schedule before deleting the old one.
        val definition = buildJobDefinition(actionId)
        val scheduler = schedulerSingleton.get()

        if (definition == null) {
            deleteJob(actionId)
            logger.info { "Job with ID '$actionId' is deleted: cron expression is empty" }
            return
        }

        val jobKey = JobKey(actionId, JOB_GROUP_NAME)
        val triggerKey = TriggerKey(actionId, JOB_GROUP_NAME)
        val previousJob = scheduler.getJobDetail(jobKey)
        val previousTrigger = scheduler.getTrigger(triggerKey)

        deleteJob(actionId)
        try {
            scheduler.scheduleJob(definition.job, definition.trigger)
            logger.info { "Job with ID '$actionId' is updated" }
        } catch (ex: Exception) {
            logger.error(ex) { "Can not update job with ID '$actionId'. Restoring the previous schedule" }
            restoreJob(scheduler, actionId, previousJob, previousTrigger)
            throw ex
        }
    }

    private fun restoreJob(
        scheduler: Scheduler,
        actionId: String,
        job: JobDetail?,
        trigger: Trigger?,
    ) {
        if (job == null || trigger == null) {
            return
        }

        try {
            scheduler.scheduleJob(job, trigger)
            logger.info { "Previous job with ID '$actionId' is restored" }
        } catch (ex: Exception) {
            logger.error(ex) { "Can not restore previous job with ID '$actionId'" }
        }
    }

    private data class JobDefinition(
        val job: JobDetail,
        val trigger: CronTrigger,
    )

    private fun deleteJob(actionId: String) {
        val scheduler = schedulerSingleton.get()
        scheduler.deleteJob(JobKey(actionId, JOB_GROUP_NAME))

        logger.info { "Job with ID '$actionId' is deleted" }
    }

    class ActionExecuteJob : Job {
        override fun execute(context: JobExecutionContext) {
            val actionId = context.jobDetail.jobDataMap.getString(JOB_DATA_KEY_ACTION_ID)
            val actionSchedulerService =
                context.jobDetail.jobDataMap[JOB_DATA_KEY_ACTION_SCHEDULER_SERVICE] as? ActionSchedulerService
            val actionExecutorService =
                context.jobDetail.jobDataMap[JOB_DATA_KEY_ACTION_EXECUTOR_SERVICE] as? ActionExecutorService

            try {
                if (actionId == null || actionSchedulerService == null || actionExecutorService == null) {
                    throw IllegalStateException(
                        "Scheduled action job data is incomplete: " +
                            "actionId='$actionId', " +
                            "actionSchedulerService=${actionSchedulerService != null}, " +
                            "actionExecutorService=${actionExecutorService != null}"
                    )
                }

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
                                logger.debug { "Scheduled atomic action '$orderId' completed. Result: $result" }
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
            } catch (ex: Throwable) {
                // Never leave a schedule without a recorded result: the executor handles
                // most failures itself, but errors outside that path must also be saved.
                logger.error(ex) { "Unexpected error when executing scheduled action '$actionId'" }
                if (actionId != null && actionSchedulerService != null) {
                    try {
                        actionSchedulerService.updateStatus(
                            UpdateStatusActionSchedulerDto(
                                actionId = actionId,
                                status = UpdateStatusInfoActionSchedulerDto.Error(ex.toException())
                            )
                        )
                    } catch (statusEx: Exception) {
                        logger.error(statusEx) {
                            "Can not save error status for scheduled action '$actionId'"
                        }
                    }
                }
            }
        }

        private fun Throwable.toException(): Exception =
            this as? Exception ?: RuntimeException(this)
    }
}
