package ru.ezhov.rocket.action.application.core.infrastructure.yml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.core.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.core.infrastructure.yml.model.ActionsDto
import ru.ezhov.rocket.action.application.core.infrastructure.yml.model.RocketActionSettingsDto
import ru.ezhov.rocket.action.application.core.infrastructure.yml.model.SettingsDto
import ru.ezhov.rocket.action.application.core.infrastructure.yml.model.SettingsValueTypeDto
import ru.ezhov.rocket.action.application.tags.application.TagServiceFactory
import java.io.File
import java.net.URI
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger {}

class YmlRocketActionSettingsRepository(
    private val uri: URI
) : RocketActionSettingsRepository {
    // TODO ezhov temporary use, will be transferred to the service after refactoring
    private val tagsService = TagServiceFactory.tagsService
    private val ymlRocketActionSettingsRepositoryOldFormat = YmlRocketActionSettingsRepositoryOldFormat(uri)
    private val mapper = ObjectMapper(YAMLFactory())
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    override fun load() {
        actions()
    }

    override fun actions(): ActionsModel {
        logger.debug { "Get actions by uri='$uri'" }

        var actions: ActionsModel

        uri.toURL().let { url ->
            actions = try {
                mapper.readValue(url, ActionsDto::class.java).toActionsModel()
            } catch (ex: Exception) {
                logger.warn(ex) { "Error when read from new settings repository, read from old" }

                ActionsModel(
                    actions = ymlRocketActionSettingsRepositoryOldFormat
                        .actions()
                        .map { it.toRocketActionSettingsModel() }
                )
            }

            logger.info { "Actions count ${actions.actions.size}" }

            fillTags(actions)
            return actions
        }
    }

    private fun fillTags(actionsModel: ActionsModel) {
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

    override fun save(actions: ActionsModel) {
        logger.debug { "Actions settings saving started. count=${actions.actions.size}" }

        mapper.writeValue(File(uri.path), ActionsDto(
            lastChangedDate = actions.lastChangedDate,
            actions = actions.actions.map { it.toRocketActionSettingsDto() }
        ))

        fillTags(actions)

        logger.info { "Actions settings saving completed. count=${actions.actions.size}" }
    }
}

private fun RocketActionSettings.toRocketActionSettingsModel(): RocketActionSettingsModel =
    RocketActionSettingsModel(
        id = this.id(),
        type = this.type().value(),
        settings = this.settings().map { (k, v) ->
            SettingsModel(
                name = k,
                value = v,
                valueType = null,
            )
        },
        actions = this.actions().map { it.toRocketActionSettingsModel() },
        tags = emptyList(),
    )

private fun ActionsDto.toActionsModel() = ActionsModel(
    lastChangedDate = this.lastChangedDate,
    actions = this.actions.map { it.toRocketActionSettingsModel() },
)

private fun RocketActionSettingsDto.toRocketActionSettingsModel(): RocketActionSettingsModel =
    RocketActionSettingsModel(
        id = this.id,
        type = this.type,
        settings = this.settings.map {
            SettingsModel(
                name = it.name,
                value = it.value,
                valueType = when (it.valueType) {
                    SettingsValueTypeDto.PLAIN_TEXT -> SettingsValueType.PLAIN_TEXT
                    SettingsValueTypeDto.MUSTACHE_TEMPLATE -> SettingsValueType.MUSTACHE_TEMPLATE
                    SettingsValueTypeDto.GROOVY_TEMPLATE -> SettingsValueType.GROOVY_TEMPLATE
                    null -> null
                },
            )
        },
        actions = this.actions.map { it.toRocketActionSettingsModel() },
        tags = this.tags,
    )

private fun RocketActionSettingsModel.toRocketActionSettingsDto(): RocketActionSettingsDto =
    RocketActionSettingsDto(
        id = this.id,
        type = this.type,
        settings = this.settings.map {
            SettingsDto(
                name = it.name,
                value = it.value,
                valueType = when (it.valueType) {
                    SettingsValueType.PLAIN_TEXT -> SettingsValueTypeDto.PLAIN_TEXT
                    SettingsValueType.MUSTACHE_TEMPLATE -> SettingsValueTypeDto.MUSTACHE_TEMPLATE
                    SettingsValueType.GROOVY_TEMPLATE -> SettingsValueTypeDto.GROOVY_TEMPLATE
                    null -> null
                },
            )
        },
        actions = this.actions.map { it.toRocketActionSettingsDto() },
        tags = this.tags,
    )
