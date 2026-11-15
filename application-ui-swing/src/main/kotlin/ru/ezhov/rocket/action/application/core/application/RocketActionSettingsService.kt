package ru.ezhov.rocket.action.application.core.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.core.domain.EngineService
import ru.ezhov.rocket.action.application.core.domain.RocketActionComponentCache
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCached
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SearchParameters
import ru.ezhov.rocket.action.application.core.event.ActionModelSavedDomainEvent
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.tags.application.TagsService
import java.awt.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

@Suppress("TooManyFunctions")
@Service
class RocketActionSettingsService(
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsRepository: RocketActionSettingsRepository,
    private val tagsService: TagsService,
    private val rocketActionContextFactory: RocketActionContextFactory,
    private val engineService: EngineService,
    private val rocketActionComponentCache: RocketActionComponentCache,
) {
    private var rocketActionAndComponents: List<RocketActionAndComponent> = emptyList()

    // One pool for the whole application: a reload does not create/tear down threads any more.
    private val fillExecutor: ExecutorService by lazy {
        val workers = minOf(20, maxOf(4, Runtime.getRuntime().availableProcessors() * 2))
        Executors.newFixedThreadPool(workers) { runnable ->
            Thread(runnable, "rocket-action-fill").apply { isDaemon = true }
        }
    }

    fun searchBy(parameters: SearchParameters): List<RocketActionSettingsModel> {
        val list = mutableListOf<RocketActionSettingsModel>()
        if (parameters.types.isEmpty()) {
            getAllActionSettings(actionsModel().actions, list) { true }
        } else {
            getAllActionSettings(actionsModel().actions, list) { action ->
                parameters.types.contains(action.type)
            }
        }

        return list
    }

    fun actionsModel(): ActionsModel = rocketActionSettingsRepository.actions()

    fun save(actions: ActionsModel) {
        rocketActionSettingsRepository.save(actions)

        fillTags(actions)

        DomainEventFactory.publisher.publish(listOf(ActionModelSavedDomainEvent(actions)))
    }

    fun getAllExistsComponents(): List<Component> = rocketActionAndComponents.map { it.component }

    /**
     * A scope that evaluates settings through the engine and owns the work reused during one reload.
     */
    fun newReloadScope(): ActionReloadScope = ActionReloadScope(engineService::processWithEngine)

    /**
     * Loads the menu and logs the reload metrics. Callers that need the swap and the MCP refresh
     * inside the same metrics line create a scope with [newReloadScope] and call the overload.
     */
    fun loadAndGetAllComponents(): List<Component> {
        val scope = newReloadScope()
        return loadAndGetAllComponents(scope).also { scope.log() }
    }

    fun loadAndGetAllComponents(scope: ActionReloadScope): List<Component> =
        ActionReloadScope.withCurrent(scope) {
            val wholeTime = System.nanoTime()

            var loadedModel: ActionsModel? = null
            val settingsReadMs = measureTimeMillis {
                loadedModel = actionsModel()
            }
            scope.settingsRead(settingsReadMs)
            val actionsModel = loadedModel!!

            logDuplicateIds(actionsModel, scope)

            val allActionSettings = mutableListOf<RocketActionSettingsModel>()
            getAllActionSettings(actionsModel.actions, allActionSettings) { true }
            scope.loaded(
                actions = allActionSettings.size,
                groups = allActionSettings.count { it.type == GroupRocketActionUi.TYPE },
            )

            val cacheFillMs = measureTimeMillis {
                fillCache(actionsModel.actions, scope)
            }
            scope.cacheFilled(cacheFillMs)

            val components = mutableListOf<RocketActionAndComponent>()
            val componentsMs = measureTimeMillis {
                for (rocketActionSettings in actionsModel.actions) {
                    try {
                        rocketActionPluginApplicationService.by(rocketActionSettings.type)
                            ?.factory(rocketActionContextFactory.context)

                        rocketActionComponentCache
                            .by(rocketActionSettings.id)
                            ?.let { action ->
                                logger.debug {
                                    "found in cache type='${rocketActionSettings.type}'" +
                                        "id='${rocketActionSettings.id}"
                                }

                                components.add(
                                    RocketActionAndComponent(
                                        rocketAction = action,
                                        component = action.origin.component()
                                    )
                                )
                            }
                    } catch (ex: Throwable) {
                        logger.error(ex) {
                            "Error when resolve component type='${rocketActionSettings.type}' " +
                                "id='${rocketActionSettings.id}'"
                        }
                        scope.failure()
                    }
                }
            }
            scope.componentsResolved(componentsMs)

            rocketActionAndComponents = components.toList()

            notifyAggregatedFailures(scope)

            val totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - wholeTime)
            logger.info { "Loading time and receiving all components '$totalMs' ms" }

            rocketActionAndComponents.map { it.component }
        }

    private fun logDuplicateIds(actionsModel: ActionsModel, scope: ActionReloadScope) {
        val duplicates = duplicatesOf(actionsModel.actions)
        if (duplicates.isEmpty()) {
            return
        }

        duplicates.forEach(scope::duplicate)
        logger.warn {
            "Duplicate action ids found. Count='${duplicates.size}'. Ids='${duplicates}'"
        }
    }

    private fun notifyAggregatedFailures(scope: ActionReloadScope) {
        val failures = scope.failures
        if (failures <= 0) {
            return
        }

        try {
            SwingUtilities.invokeLater {
                rocketActionContextFactory.context.notification().show(
                    NotificationType.ERROR,
                    "$failures actions could not be built; see the log",
                )
            }
        } catch (ex: Throwable) {
            logger.warn(ex) { "Error when notify about '$failures' failed actions" }
        }
    }

    private fun fillTags(actionsModel: ActionsModel) {
        tagsService.clear()

        fun recursion(actions: List<RocketActionSettingsModel>) {
            actions.forEach { action ->
                tagsService.add(action.id, action.tags)
                if (action.actions.isNotEmpty()) {
                    recursion(action.actions)
                }
            }
        }

        val time = measureTimeMillis {
            recursion(actionsModel.actions)
        }

        logger.info {
            "Tags filling for '${actionsModel.actions.size}' actions in '$time'ms. " +
                "Tags count is '${tagsService.count()}'"
        }
    }

    private fun fillCache(actionSettings: List<RocketActionSettingsModel>, scope: ActionReloadScope) {
        val allActionSettings = mutableListOf<RocketActionSettingsModel>()
        getAllActionSettings(actionSettings, allActionSettings) { true }

        val groupAndAnother = allActionSettings
            .groupBy { it.type == GroupRocketActionUi.TYPE }
        // Non-group actions can be created in parallel
        groupAndAnother[false]?.let { settings ->
            createAndPutToCache(settings = settings, isUsedConcurrency = true, scope = scope)
        }

        // Expand the list of groups, as we begin to create groups from child
        groupAndAnother[true]?.reversed()?.let { settings ->
            createAndPutToCache(settings = settings, isUsedConcurrency = false, scope = scope)
        }

        // Groups see the change set of this reload; only after they are built the reused
        // entries are marked as "not changed" for the next reload.
        allActionSettings.forEach { setting ->
            if (!scope.isChanged(setting.id)) {
                rocketActionComponentCache.by(setting.id)?.let { cached ->
                    rocketActionComponentCache.add(setting.id, cached.toNotChanged())
                }
            }
        }
    }

    private fun getAllActionSettings(
        actionsSettings: List<RocketActionSettingsModel>,
        allActionsForFilling: MutableList<RocketActionSettingsModel>,
        filter: (action: RocketActionSettingsModel) -> Boolean
    ) {
        actionsSettings.forEach { actionSettings ->
            if (actionSettings.actions.isEmpty() && filter(actionSettings)) {
                allActionsForFilling.add(actionSettings)
            } else {
                if (filter(actionSettings)) {
                    // must be first since the parent group must come before its children
                    allActionsForFilling.add(actionSettings)
                }
                getAllActionSettings(actionSettings.actions, allActionsForFilling, filter)
            }
        }
    }

    private fun createAndPutToCache(
        settings: List<RocketActionSettingsModel>,
        isUsedConcurrency: Boolean,
        scope: ActionReloadScope,
    ) {
        val tasks = settings.map { setting ->
            java.util.concurrent.Callable {
                try {
                    logger.debug {
                        "Start fill cache by type '${setting.type}' and id '${setting.id}'. " +
                            "isUsedConcurrency '$isUsedConcurrency'"
                    }

                    val rau = rocketActionPluginApplicationService.by(setting.type)
                        ?.factory(rocketActionContextFactory.context)

                    val cache = rocketActionComponentCache

                    if (rau != null) {
                        val rocketActionCached = cache.by(setting.id)
                        val actionSettings = setting.to(scope)
                        val isChanged =
                            rocketActionCached
                                ?.origin
                                ?.isChanged(actionSettings)
                                ?: true

                        logger.debug { "must be create '$isChanged' type='${setting.type}' id='${setting.id}'" }

                        if (isChanged) {
                            val newAction = rau.create(
                                settings = actionSettings,
                                context = rocketActionContextFactory.context
                            )

                            logger.debug { "added to cache type='${setting.type}' id='${setting.id}'" }

                            newAction?.let {
                                cache.add(setting.id, RocketActionCached.newRocketAction(it))
                                scope.markChanged(setting.id)
                            }
                        }
                    }
                } catch (ex: Throwable) {
                    logger.error(ex) {
                        "Error when create action type='${setting.type}' id='${setting.id}'"
                    }
                    scope.failure()
                }
            }
        }

        if (isUsedConcurrency) {
            try {
                val futures = fillExecutor.invokeAll(tasks, 5, TimeUnit.MINUTES)
                futures.filter { it.isCancelled }.forEach {
                    logger.error { "Task for fill cache was cancelled because it took too long" }
                }
            } catch (ex: InterruptedException) {
                logger.error(ex) { "Error when wait for fill cache rocket actions" }
                Thread.currentThread().interrupt()
            }
        } else {
            tasks.forEach { it.call() }
        }
    }

    fun actionsByIds(ids: Set<String>): List<RocketAction> =
        rocketActionComponentCache.byIds(ids).map { it.origin }

    fun actionsByContains(text: String): List<RocketAction> =
        rocketActionComponentCache
            .all()
            .map { it.origin }
            .filter { it.contains(text) }
}

/**
 * Ids that occur more than once in the model, in the order of their second occurrence.
 */
internal fun duplicatesOf(actions: List<RocketActionSettingsModel>): List<String> {
    val seen = mutableSetOf<String>()
    val duplicates = linkedSetOf<String>()

    fun visit(level: List<RocketActionSettingsModel>) {
        level.forEach { action ->
            if (!seen.add(action.id)) {
                duplicates.add(action.id)
            }
            visit(action.actions)
        }
    }

    visit(actions)
    return duplicates.toList()
}

private data class RocketActionAndComponent(
    val rocketAction: RocketActionCached,
    val component: Component,
)
