package ru.ezhov.rocket.action.plugin.openurl

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.icon.AppIcon
import ru.ezhov.rocket.action.icon.IconRepositoryFactory
import ru.ezhov.rocket.action.icon.IconService
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import ru.ezhov.rocket.action.ui.swing.common.TextFieldWithText
import java.awt.Component
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JMenu
import javax.swing.JPanel

class OpenUrlWithTextRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private val icon = IconRepositoryFactory.repository.by(AppIcon.LINK_INTACT)

    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[BASE_URL]?.takeIf { it.isNotEmpty() }?.let { baseUrl ->
            val placeholder = settings.settings()[PLACEHOLDER].orEmpty()
            val label = settings.settings()[LABEL]?.takeIf { it.isNotEmpty() } ?: baseUrl
            val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: baseUrl
            val iconUrl = settings.settings()[ICON_URL].orEmpty()

            val menu = JMenu(label)
            menu.icon = IconService().load(
                iconUrl = iconUrl,
                defaultIcon = icon
            )
            val panel = JPanel()
            panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)
            panel.add(JLabel(icon))
            val textField = TextFieldWithText(label)
            textField.columns = 10
            panel.add(textField)
            textField.toolTipText = description
            textField.addActionListener {
                textField
                    .text
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { t ->
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().browse(
                                    URI(
                                        baseUrl.replace(
                                            placeholder.toRegex(),
                                            if (settings.settings()[IS_ENCODE].toBoolean())
                                                URLEncoder.encode(t, StandardCharsets.UTF_8.toString())
                                            else t
                                        )
                                    )
                                )
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                NotificationFactory.notification.show(NotificationType.ERROR, "???????????? ???????????????? URL")
                            }
                        }
                    }
            }
            menu.add(textField)

            object : RocketAction {
                override fun contains(search: String): Boolean =
                    label.contains(search, ignoreCase = true)

                override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                    !(settings.id() == actionSettings.id() &&
                        settings.settings() == actionSettings.settings())

                override fun component(): Component = menu
            }
        }

    override fun type(): RocketActionType = RocketActionType { "OPEN_URL_WITH_TEXT" }

    override fun description(): String = "???????????????? ?????????????????? ???????????? ?? ?????????????????????????? ??????????????????"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, BASE_URL)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(LABEL, LABEL.value, "??????????????????", false),
            createRocketActionProperty(DESCRIPTION, DESCRIPTION.value, "????????????????", false),
            createRocketActionProperty(BASE_URL, BASE_URL.value, "???????????? URL", true),
            createRocketActionProperty(PLACEHOLDER, PLACEHOLDER.value, "???????????? ??????????????????????", true),
            createRocketActionProperty(ICON_URL, ICON_URL.value, "URL ????????????", false),
            createRocketActionProperty(
                key = IS_ENCODE,
                name = IS_ENCODE.value,
                description = "???????????????????? ?????? URL",
                required = false,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = false),
            )
        )
    }

    override fun name(): String = "?????????????? ???????????? ?? ????????????????????????"

    override fun icon(): Icon? = icon

    companion object {
        private val ICON_URL = RocketActionConfigurationPropertyKey("iconUrl")
        private val LABEL = RocketActionConfigurationPropertyKey("label")
        private val IS_ENCODE = RocketActionConfigurationPropertyKey("isEncode")
        private val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        private val BASE_URL = RocketActionConfigurationPropertyKey("baseUrl")
        private val PLACEHOLDER = RocketActionConfigurationPropertyKey("placeholder")
    }
}
