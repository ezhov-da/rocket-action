package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import org.ktorm.entity.toCollection
import ru.ezhov.rocket.action.application.new_.domain.ActionSettingsRepository
import ru.ezhov.rocket.action.application.new_.domain.AllActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingName
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings
import ru.ezhov.rocket.action.application.new_.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionSettingsEntity

class H2DbActionSettingsRepository(private val factory: KtormDbConnectionFactory) : ActionSettingsRepository {
    override fun all(): Either<AllActionSettingsRepositoryException, List<ActionSettings>> =
        factory.database()
            .handleErrorWith { ex ->
                Either.Left(
                    AllActionSettingsRepositoryException(
                        message = "Error get connection when get action settings",
                        cause = ex
                    )
                )
            }
            .flatMap { database ->
                try {
                    Either.Right(
                        database.actionSettings.toCollection(ArrayList()).toDomainModel()
                    )
                } catch (e: Exception) {
                    Either.Left(AllActionSettingsRepositoryException("Error when get action settings", e))
                }
            }
}

private fun List<ActionSettingsEntity>.toDomainModel() =
    this.groupBy { it.id }
        .map { (k, v) ->
            val values =
                v.associate { ActionSettingName(it.name) to ActionSettingValue(it.value) }
            ActionSettings(ActionId(k), values)
        }
