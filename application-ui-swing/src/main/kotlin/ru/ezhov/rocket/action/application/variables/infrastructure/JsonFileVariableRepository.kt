package ru.ezhov.rocket.action.application.variables.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
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

class JsonFileVariableRepository : VariableRepository {
    private val filePath =
        GeneralPropertiesRepositoryFactory
            .repository
            .asStringOrNull(UsedPropertiesName.VARIABLES_FILE_REPOSITORY_PATH)
            ?: "./variables.json"
    private val mapper = ObjectMapper().registerKotlinModule()

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
                mapper.readValue(file, JsonVariablesDto::class.java).toVariables()
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
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, variables)
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
