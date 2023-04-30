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
import ru.ezhov.rocket.action.application.core.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.application.plugin.context.RocketActionContextFactory
import java.awt.Component
import java.util.concurrent.ExecutionException
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JMenu
import javax.swing.SwingWorker

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
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: label
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menu = JMenu(label)
            menu.icon = ImageIcon(this.javaClass.getResource("/icons/load_16x16.gif"))
            menu.toolTipText = description
            GroupSwingWorker(
                parentMenu = menu,
                iconUrl = iconUrl,
                settings = settings,
            )
                .execute()

            object : RocketAction {
                override fun contains(search: String): Boolean = false

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu
            }
        }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Позволяет создать иерархию действий"

    override fun asString(): List<String> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL, "Заголовок", true),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION, "Описание", false),
            createRocketActionProperty(ICON_URL, ICON_URL, "URL иконки", false)
        )
    }

    override fun name(): String = "Группа"

    private class GroupSwingWorker(
        private val parentMenu: JMenu,
        private val iconUrl: String,
        private val settings: RocketActionSettings,
    ) : SwingWorker<List<Component?>, String?>() {

        override fun doInBackground(): List<Component> {
            return createGroup(settings.actions())
        }

        private fun createGroup(
            actionSettings: List<RocketActionSettings>,
        ): List<Component> {
            val cache = RocketActionComponentCacheFactory.cache
            val children: MutableList<Component> = ArrayList()
            for (settings in actionSettings) {
                if (settings.type().value() == TYPE) {
                    settings.settings()[LABEL]?.let { label ->
                        val description = settings.settings()[DESCRIPTION] ?: label
                        val iconUrl = settings.settings()[ICON_URL].orEmpty()

                        val menu = JMenu(label)
                        menu.icon = ImageIcon(this.javaClass.getResource("/icons/load_16x16.gif"))
                        menu.toolTipText = description
                        GroupSwingWorker(
                            parentMenu = menu,
                            iconUrl = iconUrl,
                            settings = settings,
                        ).execute()
                        createGroup(settings.actions())
                        children.add(menu)
                    }
                } else {
                    cache.by(settings.id())?.let { children.add(it.component()) }
                }
            }
            return children.toList()
        }

        override fun done() {
            try {
                val components = this.get()
                components!!.forEach { c -> parentMenu.add(c) }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
            parentMenu.icon = RocketActionContextFactory.context.icon().load(
                iconUrl,
                RocketActionContextFactory.context.icon().by(AppIcon.PROJECT)
            )
        }
    }

    override fun icon(): Icon = icon

    companion object {
        const val TYPE = "GROUP"
        private val LABEL = "label"
        private val ICON_URL = "iconUrl"
        private val DESCRIPTION = "description"
    }
}
