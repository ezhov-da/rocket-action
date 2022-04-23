package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import ru.ezhov.rocket.action.application.new_.domain.ActionSettingsRepository
import ru.ezhov.rocket.action.application.new_.domain.AllActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingName
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings
import ru.ezhov.rocket.action.application.new_.infrastructure.db.DbConnectionFactory
import java.util.UUID

class H2DbActionSettingsRepository(private val factory: DbConnectionFactory) : ActionSettingsRepository {
    override fun all(): Either<AllActionSettingsRepositoryException, List<ActionSettings>> =
        factory.connection()
            .handleErrorWith { ex ->
                Either.Left(
                    AllActionSettingsRepositoryException(
                        message = "Error get connection when get action settings",
                        cause = ex
                    )
                )
            }
            .flatMap { connection ->
                try {
                    connection.use { c ->
                        c.prepareStatement(
                            """
                                SELECT
                                    ID,
                                    NAME,
                                    "VALUE"
                                FROM ACTION_SETTINGS""".trimIndent()
                        )
                            .use { ps ->
                                ps.executeQuery()
                                    .use { rs ->
                                        val map: MutableMap<ActionId, MutableMap<ActionSettingName, ActionSettingValue?>> =
                                            mutableMapOf()

                                        while (rs.next()) {
                                            val id = ActionId(rs.getObject(1) as UUID)
                                            val name = rs.getString(2)
                                            val value = rs.getCharacterStream(3)?.readText()

                                            map.getOrPut(id) { mutableMapOf() }[ActionSettingName(name)] = value?.let { ActionSettingValue(it) }
                                        }
                                        Either.Right(map.map { (k, v) -> ActionSettings(k, v) })
                                    }
                            }
                    }
                } catch (e: Exception) {
                    Either.Left(AllActionSettingsRepositoryException("Error when get action settings", e))
                }
            }
}
