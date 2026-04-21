package ru.ezhov.rocket.action.application.core.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.RocketAction
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

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

    fun loadAndGetAllComponents(): List<Component> {
        val measureTimeMillis = measureTimeMillis {
            val actionsModel = actionsModel()
            // Tags are temporarily disabled
            // fillTags(actionsModel)
            fillCache(actionsModel.actions)
            val cache = rocketActionComponentCache
            val components = mutableListOf<RocketActionAndComponent>()
            for (rocketActionSettings in actionsModel.actions) {
                rocketActionPluginApplicationService.by(rocketActionSettings.type)
                    ?.factory(rocketActionContextFactory.context)
                    ?.let {
                        (
                            cache
                                .by(rocketActionSettings.id)
                                ?.let { action ->
                                    logger.debug {
                                        "found in cache type='${rocketActionSettings.type}'" +
                                            "id='${rocketActionSettings.id}"
                                    }

                                    RocketActionAndComponent(
                                        rocketAction = action,
                                        component = action.origin.component()
                                    )
                                }
                            )
                            ?.let { actionWithComponent -> components.add(actionWithComponent) }
                    }
            }
            rocketActionAndComponents = components.toList()
        }

        logger.info { "Loading time and receiving all components '$measureTimeMillis' ms" }

        return rocketActionAndComponents.map { it.component }
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

    private fun fillCache(actionSettings: List<RocketActionSettingsModel>) {
        val allActionSettings = mutableListOf<RocketActionSettingsModel>()
        getAllActionSettings(actionSettings, allActionSettings) { true }

        val groupAndAnother = allActionSettings
            .groupBy { it.type == GroupRocketActionUi.TYPE }
        // Non-group actions can be created in parallel
        groupAndAnother[false]?.let { settings ->
            createAndPutToCache(settings = settings, isUsedConcurrency = true)
        }

        // Expand the list of groups, as we begin to create groups from child
        groupAndAnother[true]?.reversed()?.let { settings ->
            createAndPutToCache(settings = settings, isUsedConcurrency = false)
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

    private fun createAndPutToCache(settings: List<RocketActionSettingsModel>, isUsedConcurrency: Boolean) {
        val tasks = settings.map { setting ->
            Runnable {
                logger.debug {
                    "Start fill cache by type '${setting.type}' and id '${setting.id}'. " +
                        "isUsedConcurrency '$isUsedConcurrency'"
                }

                val rau = rocketActionPluginApplicationService.by(setting.type)
                    ?.factory(rocketActionContextFactory.context)

                val cache = rocketActionComponentCache

                if (rau != null) {
                    val rocketActionCached = cache.by(setting.id)
                    val isChanged =
                        rocketActionCached
                            ?.origin
                            ?.isChanged(setting.to(engineService))
                            ?: true

                    logger.debug { "must be create '$isChanged' type='${setting.type}' id='${setting.id}'" }

                    if (isChanged) {
                        val newAction = rau.create(
                            settings = setting.to(engineService),
                            context = rocketActionContextFactory.context
                        )

                        logger.debug { "added to cache type='${setting.type}' id='${setting.id}'" }

                        newAction?.let {
                            cache.add(setting.id, RocketActionCached.newRocketAction(it))
                        }
                    } else {
                        rocketActionCached?.let { cache.add(setting.id, it.toNotChanged()) }
                    }
                }
            }
        }

        if (isUsedConcurrency) {
            val executor = Executors.newFixedThreadPool(20)
            tasks.forEach { executor.execute(it) }
            executor.shutdown()

            try {
                if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                    System.err.println("Tasks took too long; forcing shutdown.")
                    executor.shutdownNow()
                }
            } catch (ex: Exception) {
                logger.error(ex) { "Error when shutdown executor for fill cache rocket actions" }
                executor.shutdownNow()
                Thread.currentThread().interrupt()
            }
        } else {
            tasks.forEach { it.run() }
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

private data class RocketActionAndComponent(
    val rocketAction: RocketActionCached,
    val component: Component,
)
