package ru.ezhov.rocket.action.application.core.application

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.core.event.RocketActionSettingsCreatedDomainEvent
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.time.LocalDateTime
import java.util.*

// TODO ezhov причесать
@Service
class CreateRocketActionSettingsService(
    private val rocketActionSettingsService: RocketActionSettingsService,
    private val notificationService: NotificationService,
) {
    fun create(groupId: String, type: String, params: Map<String, String>, tags: String? = null) {
        val model = rocketActionSettingsService.actionsModel()

        model.lastChangedDate = LocalDateTime.now()
        val actions = model.actions.toMutableList()

        val groupAction = actions.firstOrNull { it.id == groupId }
            ?: throw IllegalArgumentException("Not found group by ID '$groupId'")

        val actionsInGroup = groupAction.actions.toMutableList()

        val rocketActionSettingsModel =
            RocketActionSettingsModel(
                id = UUID.randomUUID().toString(),
                type = type,
                settings = params.map { (k, v) ->
                    SettingsModel(
                        name = k,
                        value = v,
                        valueType = SettingsValueType.PLAIN_TEXT,
                    )
                },
                actions = emptyList(),
                tags = tags?.split(",")?.map { it.trim() }.orEmpty(),
            )

        actionsInGroup.add(rocketActionSettingsModel)

        groupAction.actions = actionsInGroup

        rocketActionSettingsService.save(model)

        notificationService.show(NotificationType.INFO, "Action created")

        DomainEventFactory.publisher.publish(
            listOf(
                RocketActionSettingsCreatedDomainEvent(
                    groupId = groupAction.id,
                    rocketActionSettingsModel = rocketActionSettingsModel
                )
            )
        )
    }
}
