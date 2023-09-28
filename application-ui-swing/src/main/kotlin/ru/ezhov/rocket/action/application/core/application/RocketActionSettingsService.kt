package ru.ezhov.rocket.action.application.core.application

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import java.awt.Component

private val logger = KotlinLogging.logger {}

class RocketActionSettingsService(
    private val rocketActionPluginApplicationService: RocketActionPluginApplicationService,
    private val rocketActionSettingsRepository: RocketActionSettingsRepository,
) {
    private var rocketActionAndComponents: List<RocketActionAndComponent> = emptyList()

    fun actionsModel(): ActionsModel = rocketActionSettingsRepository.actions()

    fun save(actions: ActionsModel) {
        rocketActionSettingsRepository.save(actions)
    }

    fun getAllExistsComponents(): List<Component> = rocketActionAndComponents.map { it.component }

    fun loadAndGetAllComponents(): List<Component> {
        val actionSettings = actionsModel()
        fillCache(actionSettings.actions)
        val cache = RocketActionComponentCacheFactory.cache
        val components = mutableListOf<RocketActionAndComponent>()
        for (rocketActionSettings in actionSettings.actions) {
            rocketActionPluginApplicationService.by(rocketActionSettings.type)
                ?.factory(RocketActionContextFactory.context)
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
                                    action,
                                    action.component()
                                )
                            }
                            ?: run {
                                logger.debug {
                                    "not found in cache type='${rocketActionSettings.type}'" +
                                        "id='${rocketActionSettings.id}. Create component"
                                }

                                it.create(
                                    settings = rocketActionSettings.to(),
                                    context = RocketActionContextFactory.context
                                )?.let { action ->
                                    RocketActionAndComponent(
                                        action,
                                        action.component()
                                    )
                                }
                            }
                        )
                        ?.let { actionWithComponent ->
                            components.add(actionWithComponent)
                        }
                }
        }
        rocketActionAndComponents = components.toList()
        return rocketActionAndComponents.map { it.component }
    }

    private fun fillCache(actionSettings: List<RocketActionSettingsModel>) {
        RocketActionComponentCacheFactory
            .cache
            .let { cache ->
                for (rocketActionSettings in actionSettings) {
                    val rau = rocketActionPluginApplicationService.by(rocketActionSettings.type)
                        ?.factory(RocketActionContextFactory.context)
                    if (rau != null) {
                        if (rocketActionSettings.type != GroupRocketActionUi.TYPE) {
                            val mustBeCreate = cache
                                .by(rocketActionSettings.id)
                                ?.isChanged(rocketActionSettings.to()) ?: true

                            logger.debug {
                                "must be create '$mustBeCreate' type='${rocketActionSettings.type}'" +
                                    "id='${rocketActionSettings.id}'"
                            }

                            if (mustBeCreate) {
                                rau.create(
                                    settings = rocketActionSettings.to(),
                                    context = RocketActionContextFactory.context
                                )
                                    ?.let { action ->
                                        logger.debug {
                                            "added to cache type='${rocketActionSettings.type}'" +
                                                "id='${rocketActionSettings.id}'"
                                        }

                                        cache.add(
                                            rocketActionSettings.id,
                                            action
                                        )
                                    }
                            }
                        } else {
                            if (rocketActionSettings.actions.isNotEmpty()) {
                                fillCache(rocketActionSettings.actions)
                            }
                        }
                    }
                }
            }
    }

    fun actionsByIds(ids: Set<String>): List<RocketAction> =
        RocketActionComponentCacheFactory.cache.byIds(ids)

    fun actionsByContains(text: String): List<RocketAction> =
        RocketActionComponentCacheFactory.cache
            .all()
            .filter { it.contains(text) }
}

private data class RocketActionAndComponent(
    val rocketAction: RocketAction,
    val component: Component,
)
