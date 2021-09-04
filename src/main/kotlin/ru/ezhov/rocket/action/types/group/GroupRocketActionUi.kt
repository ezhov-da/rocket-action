package ru.ezhov.rocket.action.types.group

import ru.ezhov.rocket.action.api.Action
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.SearchableAction
import ru.ezhov.rocket.action.domain.RocketActionUiRepository
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.infrastructure.RocketActionComponentCacheFactory
import ru.ezhov.rocket.action.infrastructure.RocketActionUiRepositoryFactory
import ru.ezhov.rocket.action.types.AbstractRocketAction
import ru.ezhov.rocket.action.types.ConfigurationUtil
import java.awt.Component
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import javax.swing.ImageIcon
import javax.swing.JMenu
import javax.swing.SwingWorker

class GroupRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Action {
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
        menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        GroupSwingWorker(menu, settings).execute()
        return object : Action {
            override fun action(): SearchableAction = object : SearchableAction {
                override fun contains(search: String): Boolean = false
            }

            override fun component(): Component = menu
        }
    }

    override fun type(): String = TYPE

    override fun description(): String = "description"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
                createRocketActionProperty(LABEL, LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, DESCRIPTION, "TEST", true),
                createRocketActionProperty(ICON_URL, ICON_URL, "URL for icon", false)
        )
    }

    override fun name(): String = "Группа"

    private class GroupSwingWorker(private val parentMenu: JMenu, private val settings: RocketActionSettings) : SwingWorker<List<Component?>, String?>() {
        @Throws(Exception::class)
        override fun doInBackground(): List<Component?> {
            val rocketActionUiRepository: RocketActionUiRepository = RocketActionUiRepositoryFactory.repository
            return createGroup(rocketActionUiRepository, settings.actions(), parentMenu)
        }

        private fun createGroup(rocketActionUiRepository: RocketActionUiRepository, actionSettings: List<RocketActionSettings?>?, parent: JMenu): List<Component?> {
            val cache = RocketActionComponentCacheFactory.cache
            val children: MutableList<Component?> = ArrayList()
            for (settings in actionSettings!!) {
                if (settings!!.type() == TYPE) {
                    val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
                    menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
                    menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
                    GroupSwingWorker(menu, settings).execute()
                    createGroup(rocketActionUiRepository, settings.actions(), menu)
                    children.add(menu)
                } else {
                    cache.by(settings.id())?.let { children.add(it.component()) }
                }
            }
            return children
        }

        override fun done() {
            try {
                val components = this.get()
                components!!.forEach(Consumer { c: Component? -> parentMenu.add(c) })
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
            parentMenu.icon = IconService().load(
                    settings.settings()[ICON_URL].orEmpty(),
                    IconRepositoryFactory.repository.by(AppIcon.PROJECT)
            )
        }
    }

    companion object {
        const val TYPE = "GROUP"
        private const val LABEL = "label"
        private const val ICON_URL = "iconUrl"
        private const val DESCRIPTION = "description"
    }
}