package ru.ezhov.rocket.action.application.new_.infrastructure.db.h2

import arrow.core.Either
import arrow.core.flatMap
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.removeIf
import org.ktorm.entity.toCollection
import org.ktorm.entity.toList
import ru.ezhov.rocket.action.application.new_.domain.ActionSettingsRepository
import ru.ezhov.rocket.action.application.new_.domain.ActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.AllActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.SaveActionSettingsRepositoryException
import ru.ezhov.rocket.action.application.new_.domain.model.ActionId
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingName
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettingValue
import ru.ezhov.rocket.action.application.new_.domain.model.ActionSettings
import ru.ezhov.rocket.action.application.new_.infrastructure.db.KtormDbConnectionFactory
import ru.ezhov.rocket.action.application.new_.infrastructure.db.database
import ru.ezhov.rocket.action.application.new_.infrastructure.db.h2.dto.ActionSettingsEntity

class H2DbActionSettingsRepository(
    private val factory: KtormDbConnectionFactory
) : ActionSettingsRepository {
    override fun all(): Either<AllActionSettingsRepositoryException, List<ActionSettings>> =
        factory.database { e ->
            AllActionSettingsRepositoryException(
                message = "Error get connection when get action settings",
                cause = e
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

    override fun settings(id: ActionId): Either<ActionSettingsRepositoryException, ActionSettings?> =
        factory.database { e ->
            ActionSettingsRepositoryException(
                message = "Error get connection when get action settings by id='${id.value}",
                cause = e
            )
        }
            .flatMap { database ->
                try {
                    Either.Right(
                        database
                            .actionSettings
                            .filter { it.id eq id.value }
                            .toList()
                            .toDomainModel()
                            .firstOrNull()
                    )
                } catch (e: Exception) {
                    Either.Left(ActionSettingsRepositoryException("Error when get action settings by id='${id.value}'", e))
                }
            }

    override fun save(actionSettings: ActionSettings): Either<SaveActionSettingsRepositoryException, Unit> =
        factory.database { e ->
            SaveActionSettingsRepositoryException(
                message = "Error when save action settings by id='${actionSettings.id.value}",
                cause = e
            )
        }
            .flatMap { database ->
                try {
                    database.useTransaction {
                        database.actionSettings.removeIf { ast -> ast.id eq actionSettings.id.value }
                        actionSettings.map.forEach { m ->
                            database.actionSettings.add(ActionSettingsEntity {
                                id = actionSettings.id.value
                                name = m.key.value
                                value = m.value?.value
                            })
                        }
                    }

                    Either.Right(Unit)
                } catch (e: Exception) {
                    Either.Left(
                        SaveActionSettingsRepositoryException(
                            message = "Error when save action settings by id='${actionSettings.id.value}'",
                            cause = e
                        )
                    )
                }
            }

    private fun List<ActionSettingsEntity>.toDomainModel() =
        this.groupBy { it.id }
            .map { (k, v) ->
                val values =
                    v.associate { ActionSettingName(it.name) to it.value?.let { v -> ActionSettingValue(v) } }
                ActionSettings(ActionId(k), values)
            }
}
