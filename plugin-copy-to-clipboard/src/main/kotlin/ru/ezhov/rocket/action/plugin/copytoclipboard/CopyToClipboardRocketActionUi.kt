package ru.ezhov.rocket.action.plugin.copytoclipboard

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.Icon
import javax.swing.JMenuItem

class CopyToClipboardRocketActionUi : AbstractRocketAction(), RocketActionPlugin {

    private val icon = IconRepositoryFactory.repository.by(AppIcon.CLIPBOARD)

    companion object {
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val TEXT = RocketActionConfigurationPropertyKey("text")
    }

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[TEXT]?.takeIf { it.isNotEmpty() }?.let { text ->
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() }
                ?: text
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() }
                ?: text
            val menuItem = JMenuItem(label)
            menuItem.icon = icon
            menuItem.toolTipText = description
            menuItem.addActionListener {
                val defaultToolkit = Toolkit.getDefaultToolkit()
                val clipboard = defaultToolkit.systemClipboard
                clipboard.setContents(StringSelection(text), null)
                NotificationFactory.notification.show(NotificationType.INFO, "?????????? ???????????????????? ?? ??????????")
            }

            object : RocketAction {
                override fun contains(search: String): Boolean =
                    text.contains(search, ignoreCase = true)
                        .or(label.contains(search, ignoreCase = true))
                        .or(description.contains(search, ignoreCase = true))

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menuItem
            }
        }

    override fun type(): RocketActionType = RocketActionType { "COPY_TO_CLIPBOARD" }

    override fun description(): String {
        return "?????????????????? ?????????????????????? ?????????? ?? ??????????"
    }

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
        LABEL, TEXT
    )

    override fun name(): String = "???????????????????? ?? ??????????"

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL.value, "?????????? ?????? ??????????????????????", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "????????????????", false),
            createRocketActionProperty(TEXT, TEXT.value, "?????????? ?????? ?????????????????????? ?? ??????????", true)
        )
    }

    override fun icon(): Icon? = icon

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this
}