package ru.ezhov.rocket.action.application.plugin.group

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.application.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import java.awt.Component
import java.util.concurrent.ExecutionException
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JMenu
import javax.swing.SwingWorker

class GroupRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    private val icon = IconRepositoryFactory.repository.by(AppIcon.PROJECT)

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }?.let { label ->
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: label
            val iconUrl = settings.settings()[ICON_URL].orEmpty()
            val menu = JMenu(label)
            menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
            menu.toolTipText = description
            GroupSwingWorker(menu, iconUrl, settings).execute()

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

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL.value, "Заголовок", true),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "Описание", false),
            createRocketActionProperty(ICON_URL, ICON_URL.value, "URL иконки", false)
        )
    }

    override fun name(): String = "Группа"

    private class GroupSwingWorker(
        private val parentMenu: JMenu,
        private val iconUrl: String,
        private val settings: RocketActionSettings
    ) : SwingWorker<List<Component?>, String?>() {
        @Throws(Exception::class)
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
                        menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
                        menu.toolTipText = description
                        GroupSwingWorker(menu, iconUrl, settings).execute()
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
            parentMenu.icon = IconService().load(
                iconUrl,
                IconRepositoryFactory.repository.by(AppIcon.PROJECT)
            )
        }
    }

    override fun icon(): Icon? = icon

    companion object {
        const val TYPE = "GROUP"
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
    }
}