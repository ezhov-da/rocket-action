package ru.ezhov.rocket.action.application.variables.application

import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.applicationConfiguration.application.ConfigurationApplication
import ru.ezhov.rocket.action.application.encryption.application.EncryptionServiceFactory
import ru.ezhov.rocket.action.application.encryption.domain.EncryptionService
import ru.ezhov.rocket.action.application.encryption.domain.model.Algorithm
import ru.ezhov.rocket.action.application.variables.domain.VariableRepository
import ru.ezhov.rocket.action.application.variables.domain.model.Encryption
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import ru.ezhov.rocket.action.application.variables.domain.model.Variables

private val logger = KotlinLogging.logger { }

@Service
class VariablesApplication(
    private val variableRepository: VariableRepository,
    private val configurationApplication: ConfigurationApplication,
) {
    private val defaultAlgorithm = Algorithm.BLOWFISH
    private val encryptionService = EncryptionServiceFactory.get(defaultAlgorithm)!!

    fun all(): VariablesDto {
        val key = configurationApplication.all().variablesKey
        val originVariables = variableRepository.all()
        return if (originVariables.encryption != null) {
            val algorithm = Algorithm.of(originVariables.encryption.algorithm)
            if (algorithm != null) {
                val service = EncryptionServiceFactory.get(algorithm)
                if (service != null) {
                    decode(originVariables, key, service)
                } else {
                    logger.warn { "Encryption service not found by algorithm '$algorithm'" }
                    originVariables.toVariablesDto(key)
                }
            } else {
                logger.warn { "Algorithm not found by algorithm '${originVariables.encryption.algorithm}'" }
                originVariables.toVariablesDto(key)
            }
        } else {
            logger.warn { "User variables is not encoded! Please save variables with encode" }
            originVariables.toVariablesDto(key)
        }
    }

    private fun decode(variables: Variables, key: String, encryptionService: EncryptionService): VariablesDto =
        VariablesDto(
            key = key,
            variables = variables.variables.map { v ->
                val value = if (v.type == VariableType.APPLICATION) {
                    encryptionService.decrypt(v.value, key)
                } else {
                    v.value
                }

                VariableDto(
                    name = v.name,
                    value = value,
                    description = v.description,
                    type = v.type,
                )

            }
        )

    private fun encode(variables: VariablesDto, encryptionService: EncryptionService): Variables =
        Variables(
            encryption = Encryption(algorithm = defaultAlgorithm.name),
            variables = variables.variables.map { v ->
                Variable(
                    name = v.name,
                    value = encryptionService.encrypt(source = v.value, key = variables.key),
                    description = v.description,
                    type = v.type,
                )

            }
        )

    fun save(variables: VariablesDto) {
        val applicationConfig = configurationApplication.all()
        applicationConfig.variablesKey = variables.key
        val resultVariables = encode(variables = variables, encryptionService = encryptionService)

        variableRepository.save(resultVariables)
        configurationApplication.save(applicationConfig)
    }

    fun updateVariable(key: String, value: String) {
        val configuration = configurationApplication.all()
        val all = all().variables.filter { it.type == VariableType.APPLICATION }.toMutableList()
        val variableForUpdate = all()
            .variables
            .firstOrNull { it.name == key && it.type == VariableType.APPLICATION }
            ?: throw RuntimeException("Not found application variable '$key'")

        val withNewValue = variableForUpdate.updateValue(value)
        all.remove(variableForUpdate)
        all.add(withNewValue)

        val resultVariables = encode(
            variables = VariablesDto(
                key = configuration.variablesKey,
                variables = all
            ), encryptionService = encryptionService
        )

        variableRepository.save(resultVariables)
    }
}

private fun Variables.toVariablesDto(key: String): VariablesDto =
    VariablesDto(
        key = key,
        variables = this.variables.map {
            VariableDto(
                name = it.name,
                value = it.value,
                description = it.description,
                type = it.type,
            )
        }
    )
