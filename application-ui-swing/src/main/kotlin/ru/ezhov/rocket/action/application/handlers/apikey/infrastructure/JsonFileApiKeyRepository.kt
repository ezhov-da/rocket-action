package ru.ezhov.rocket.action.application.handlers.apikey.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.handlers.apikey.domain.ApiKeyRepository
import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKey
import ru.ezhov.rocket.action.application.handlers.apikey.domain.model.ApiKeys
import ru.ezhov.rocket.action.application.handlers.apikey.infrastructure.model.JsonApiKeysDto
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class JsonFileApiKeyRepository(
    generalPropertiesRepository: GeneralPropertiesRepository,
    private val objectMapper: ObjectMapper,
) : ApiKeyRepository {
    private val filePath =
        generalPropertiesRepository
            .asStringOrNull(UsedPropertiesName.API_KEYS_FILE_REPOSITORY_PATH)
            ?: "./.rocket-action/api-keys.json"

    override fun all(): ApiKeys {
        val file = file()
        return try {
            if (file.exists()) {
                objectMapper.readValue(file, JsonApiKeysDto::class.java).toApiKeys()
            } else {
                ApiKeys.EMPTY
            }
        } catch (ex: Exception) {
            logger.warn(ex) { "Error when read api keys from file '$filePath'. Empty list returned" }
            ApiKeys.EMPTY
        }
    }

    private fun file(): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile.mkdirs()
        }

        return file
    }

    override fun save(apiKeys: ApiKeys) {
        val file = file()
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, apiKeys)
    }
}

private fun JsonApiKeysDto.toApiKeys() =
    ApiKeys(
        keys = this.keys.map {
            ApiKey(
                value = it.value,
                description = it.description,
            )
        }
    )
