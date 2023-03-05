package ru.ezhov.rocket.action.application.variables.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.variables.domain.VariableRepository
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables
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
        return if (file.exists()) {
            try {
                Variables(
                    (
                        System.getenv().map {
                            Variable(
                                name = it.key.toString(),
                                value = it.value.toString(),
                                type = VariableType.ENVIRONMENT,
                            )
                        }.toMutableList() +
                            System.getProperties().entries.map {
                                Variable(
                                    name = it.key.toString(),
                                    value = it.value.toString(),
                                    type = VariableType.PROPERTIES,
                                )
                            }.toMutableList() +
                            mapper.readValue(file, JsonVariablesDto::class.java)
                                .toVariables().variables.toMutableList()
                        )
                )
            } catch (ex: Exception) {
                logger.error(ex) { "Error when read variables from file '$filePath'" }
                Variables.EMPTY
            }
        } else {
            return Variables.EMPTY
        }
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
        mapper.writeValue(file, variables)
    }
}

private fun JsonVariablesDto.toVariables() =
    Variables(
        variables = this.variables.map {
            Variable(
                name = it.name,
                description = it.description,
                value = it.value,
                type = VariableType.APPLICATION
            )
        }
    )
