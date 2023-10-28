package ru.ezhov.rocket.action.application.variables.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.domain.VariableRepository
import ru.ezhov.rocket.action.application.variables.domain.model.Encryption
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables
import ru.ezhov.rocket.action.application.variables.infrastructure.model.JsonEncryptionDto
import ru.ezhov.rocket.action.application.variables.infrastructure.model.JsonVariablesDto
import java.io.File

private val logger = KotlinLogging.logger {}

@Component
class JsonFileVariableRepository(
    generalPropertiesRepository: GeneralPropertiesRepository,
    private val objectMapper: ObjectMapper,
) : VariableRepository {
    private val filePath =
        generalPropertiesRepository
            .asStringOrNull(UsedPropertiesName.VARIABLES_FILE_REPOSITORY_PATH)
            ?: "./variables.json"

    override fun all(): Variables {
        val file = file()

        val systemEnv = System.getenv().map {
            Variable(
                name = it.key.toString(),
                value = it.value.toString(),
                type = VariableType.ENVIRONMENT,
            )
        }

        val systemProp = System.getProperties().entries.map {
            Variable(
                name = it.key.toString(),
                value = it.value.toString(),
                type = VariableType.PROPERTIES,
            )
        }

        val userVar = try {
            if (file.exists()) {
                objectMapper.readValue(file, JsonVariablesDto::class.java).toVariables()
            } else {
                Variables.EMPTY
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Error when read variables from file '$filePath'. Empty list returned" }
            Variables.EMPTY
        }

        val variables = systemEnv.toMutableList() + systemProp.toMutableList() + userVar.variables.toMutableList()
        return Variables(encryption = userVar.encryption, variables = variables)
    }

    private fun file(): File {
        val file = File(filePath)
        if (!file.exists()) {
            file.parentFile.mkdirs()

        }

        return file
    }

    override fun save(variables: Variables) {
        val file = file()
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, variables)
    }
}

private fun JsonVariablesDto.toVariables() =
    Variables(
        encryption = this.encryption?.toEncryption(),
        variables = this.variables.map {
            Variable(
                name = it.name,
                description = it.description,
                value = it.value,
                type = VariableType.APPLICATION
            )
        }
    )

private fun JsonEncryptionDto.toEncryption() =
    Encryption(algorithm = this.algorithm)
