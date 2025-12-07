package ru.ezhov.rocket.action.application.variables.infrastructure.manager

import mu.KotlinLogging
import org.linguafranca.pwdb.kdbx.KdbxCreds
import org.linguafranca.pwdb.kdbx.jackson.JacksonDatabase
import org.linguafranca.pwdb.kdbx.jackson.JacksonGroup
import ru.ezhov.rocket.action.application.applicationConfiguration.domain.model.VariablesManager
import ru.ezhov.rocket.action.application.variables.domain.model.Variable
import ru.ezhov.rocket.action.application.variables.domain.model.VariableType
import java.io.File

private val logger = KotlinLogging.logger { }

class KeePassManagerRepository {
    fun variables(
        password: String,
        manager: VariablesManager.KeePassVariablesManager
    ): List<Variable> {
        File(manager.dbPath).inputStream().use { fileIs ->
            val database = JacksonDatabase.load(KdbxCreds(password.toByteArray()), fileIs)

            val groups = mutableListOf<JacksonGroup>()

            fun recursiveGroups(group: JacksonGroup) {
                group.groups.forEach { recursiveGroups(it) }
                groups.add(group)
            }

            recursiveGroups(database.rootGroup)

            val entries = groups.map { g -> g.entries }.flatten()

            val regex = manager.variableRegExp.toRegex()
            return try {
                entries
                    .distinctBy { it.uuid }
                    .filter { it.title != null }
                    .mapNotNull {
                        regex
                            .find(it.title)
                            ?.groups
                            ?.get(1)
                            ?.value
                            ?.let { varName ->
                                listOf(
                                    Variable(
                                        name = "${varName}_USERNAME",
                                        value = it.username.orEmpty(),
                                        type = VariableType.KEE_PASS,
                                    ),

                                    Variable(
                                        name = "${varName}_PASSWORD",
                                        value = it.password.orEmpty(),
                                        type = VariableType.KEE_PASS,
                                    )
                                )
                            }
                    }
                    .flatten()
            } catch (ex: Exception) {
                logger.error(ex) { "Error when read KeePass database by $manager" }
                emptyList()
            }
        }
    }
}
