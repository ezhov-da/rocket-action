package ru.ezhov.rocket.action.application.plugin.group

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPluginInfo
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.application.CreateMenuDomainEvent
import ru.ezhov.rocket.action.application.RestoreMenuDomainEvent
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCachedState
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.event.domain.DomainEvent
import ru.ezhov.rocket.action.application.event.domain.DomainEventSubscriber
import ru.ezhov.rocket.action.application.event.infrastructure.DomainEventFactory
import java.awt.Component
import java.util.*
import javax.swing.Icon
import javax.swing.JMenu

private val logger = KotlinLogging.logger { }

class GroupRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply {
            actionContext = context
        }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply {
            actionContext = context
        }

    override fun info(): RocketActionPluginInfo = object : RocketActionPluginInfo {
        override fun version(): String = Properties().let {
            it.load(this.javaClass.getResourceAsStream("/general.properties"))
            it.getProperty("rocket.action.version")
        }

        override fun author(): String = "DEzhov"

        override fun link(): String? = null
    }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            val childrenIds = settings.actions().map { it.id() }.toSet()
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: label
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val showNumberOfChildren = settings.settings()[SHOW_NUMBER_OF_CHILDREN]?.toBooleanStrictOrNull() ?: false

            val labelFinal = if (showNumberOfChildren) {
                "$label (${childrenIds.size})"
            } else {
                label
            }

            val menu = JMenu(labelFinal)
            menu.icon = context.icon().load(
                iconUrl = iconUrl,
                defaultIcon = context.icon().by(AppIcon.PROJECT)
            )
            menu.toolTipText = description

            val cache = RocketActionComponentCacheFactory.cache
            settings.actions().forEach { settings ->
                // the mandatory presence of a child component controls the creation of groups last
                menu.add(cache.by(settings.id())!!.origin.component())
            }

            DomainEventFactory.subscriberRegistrar.subscribe(
                object : DomainEventSubscriber {
                    override fun handleEvent(event: DomainEvent) {
                        if (menu.menuComponents.size != settings.actions().size) {
                            menu.removeAll()
                            logger.debug { "Refill group menu '$labelFinal' by event ${RestoreMenuDomainEvent::class.java.name}" }

                            settings.actions().forEach { settings ->
                                // the mandatory presence of a child component controls the creation of groups last
                                menu.add(cache.by(settings.id())!!.origin.component())
                            }
                        }
                    }

                    override fun subscribedToEventType(): List<Class<*>> =
                        listOf(
                            CreateMenuDomainEvent::class.java,
                            RestoreMenuDomainEvent::class.java,
                        )
                }
            )

            object : RocketAction {
                override fun contains(search: String): Boolean = false

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings()) ||
                        childrenIds.size != actionSettings.actions().size ||
                        cache.byIds(childrenIds).any { it.state == RocketActionCachedState.CHANGED_SINCE_LAST_LOAD }

                override fun component(): Component = menu
            }
        }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Allows you to create a hierarchy of actions"

    override fun asString(): List<String> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Header", required = true),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION,
                description = "Description",
                required = false
            ),
            createRocketActionProperty(key = ICON_URL, name = ICON_URL, description = "Icon URL", required = false),
            createRocketActionProperty(
                key = SHOW_NUMBER_OF_CHILDREN,
                name = "Show number of children",
                description = "Show number of children",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = true)
            )
        )
    }

    override fun name(): String = "Group"

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.PROJECT)

    companion object {
        const val TYPE = "GROUP"
        private const val LABEL = "label"
        private const val ICON_URL = "iconUrl"
        private const val DESCRIPTION = "description"
        private const val SHOW_NUMBER_OF_CHILDREN = "showNumberOfChildren"
    }
}
