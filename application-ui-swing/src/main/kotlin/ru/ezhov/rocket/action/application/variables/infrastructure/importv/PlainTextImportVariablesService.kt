package ru.ezhov.rocket.action.application.variables.infrastructure.importv

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.variables.domain.importv.ImportVariablesService
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType

private val logger = KotlinLogging.logger { }

class PlainTextImportVariablesService(
    private val text: String,
) : ImportVariablesService {
    override fun variables(): List<Variable> =
        text
            .split("\n")
            .mapNotNull {
                when (val firstEqIndex = it.indexOf("=")) {
                    -1 -> null
                    else -> {
                        try {
                            val name = it.substring(0, firstEqIndex)
                            val value = it.substring(firstEqIndex + 1, it.length)

                            Variable(
                                name = name,
                                value = value,
                                description = "",
                                type = VariableType.APPLICATION
                            )
                        } catch (ex: Exception) {
                            logger.warn { "Wrong variable format '$it'" }
                            null
                        }
                    }
                }
            }
}
