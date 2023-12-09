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
            fillTags(actionsModel)
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

        val groupAndAnother = allActionSettings.groupBy { it.type == GroupRocketActionUi.TYPE }
        groupAndAnother[false]?.forEach { settings -> createAndPutToCache(settings) }
        // expand the list of groups, as we begin to create groups from child
        groupAndAnother[true]?.reversed()?.forEach { settings -> createAndPutToCache(settings) }
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

    private fun createAndPutToCache(settings: RocketActionSettingsModel) {
        val rau = rocketActionPluginApplicationService.by(settings.type)
            ?.factory(rocketActionContextFactory.context)
        val cache = rocketActionComponentCache
        if (rau != null) {
            val rocketActionCached = cache.by(settings.id)
            val isChanged =
                rocketActionCached
                    ?.origin
                    ?.isChanged(settings.to(engineService))
                    ?: true

            logger.debug { "must be create '$isChanged' type='${settings.type}' id='${settings.id}'" }

            if (isChanged) {
                val newAction = rau.create(
                    settings = settings.to(engineService),
                    context = rocketActionContextFactory.context
                )

                logger.debug { "added to cache type='${settings.type}' id='${settings.id}'" }

                newAction?.let {
                    cache.add(settings.id, RocketActionCached.newRocketAction(it))
                }
            } else {
                rocketActionCached?.let { cache.add(settings.id, it.toNotChanged()) }
            }
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
