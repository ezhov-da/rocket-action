package ru.ezhov.rocket.action.types

import ru.ezhov.rocket.action.RocketActionUiRepository
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionUi
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository
import java.awt.Component
import java.util.concurrent.ExecutionException
import java.util.function.Consumer
import javax.swing.ImageIcon
import javax.swing.JMenu
import javax.swing.SwingWorker

class GroupRocketActionUi : AbstractRocketAction() {
    override fun create(settings: RocketActionSettings): Component {
        val menu = JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL))
        menu.icon = ImageIcon(this.javaClass.getResource("/load_16x16.gif"))
        menu.toolTipText = ConfigurationUtil.getValue(settings.settings(), DESCRIPTION)
        GroupSwingWorker(menu, settings).execute()
        return menu
    }

    override fun type(): String {
        return TYPE
    }

    override fun description(): String {
        return "description"
    }

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
            val rocketActionUiRepository: RocketActionUiRepository = ReflectionRocketActionUiRepository() //TODO: сделать нормально
            rocketActionUiRepository.load()
            return createGroup(rocketActionUiRepository, settings.actions(), parentMenu)
        }

        private fun createGroup(rocketActionUiRepository: RocketActionUiRepository, actionSettings: List<RocketActionSettings?>?, parent: JMenu): List<Component?> {
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
                    rocketActionUiRepository.by(settings.type())
                            ?.let { rocketActionUi: RocketActionUi ->
                                children.add(rocketActionUi.create(settings))
                            }
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
        private const val TYPE = "GROUP"
        private const val LABEL = "label"
        private const val ICON_URL = "iconUrl"
        private const val DESCRIPTION = "description"
    }
}