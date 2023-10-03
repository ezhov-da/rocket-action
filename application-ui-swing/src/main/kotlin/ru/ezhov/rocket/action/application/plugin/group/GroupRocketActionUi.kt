package ru.ezhov.rocket.action.application.plugin.group

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionCachedState
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import java.awt.Component
import javax.swing.Icon
import javax.swing.JMenu

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

    private val icon = RocketActionContextFactory.context.icon().by(AppIcon.PROJECT)

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            val childrenIds = settings.actions().map { it.id() }.toSet()
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: label
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menu = JMenu(label)
            menu.icon = RocketActionContextFactory.context.icon().load(
                iconUrl = iconUrl,
                defaultIcon = RocketActionContextFactory.context.icon().by(AppIcon.PROJECT)
            )
            menu.toolTipText = description

            val cache = RocketActionComponentCacheFactory.cache
            settings.actions().forEach { settings ->
                // the mandatory presence of a child component controls the creation of groups last
                menu.add(cache.by(settings.id())!!.origin.component())
            }

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
            createRocketActionProperty(LABEL, LABEL, "Header", true),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Description", false),
            createRocketActionProperty(ICON_URL, ICON_URL, "Icon URL", false)
        )
    }

    override fun name(): String = "Group"

    override fun icon(): Icon = icon

    companion object {
        const val TYPE = "GROUP"
        private const val LABEL = "label"
        private const val ICON_URL = "iconUrl"
        private const val DESCRIPTION = "description"
    }
}
