package ru.ezhov.rocket.action.plugin.script.kotlin.ui

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
import java.awt.Component
import javax.swing.Icon

class KotlinScriptRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    override fun factory(): RocketActionFactoryUi = this

    override fun configuration(): RocketActionConfiguration = this

    override fun create(settings: RocketActionSettings): RocketAction? =
        settings.settings()[SCRIPT]
            ?.takeIf { it.isNotEmpty() }
            ?.let { script ->
                settings.settings()[LABEL]
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { label ->
                        val description = settings.settings()[DESCRIPTION]?.takeIf { it.isNotEmpty() } ?: script
                        val menu = ScriptMenu(
                            label = label,
                            script = script,
                            description = description,
                            executeOnLoad = settings.settings()[EXECUTE_ON_LOAD].toBoolean(),
                        )

                        object : RocketAction {
                            override fun contains(search: String): Boolean =
                                label.contains(search, ignoreCase = true)
                                    .or(description.contains(search, ignoreCase = true))

                            override fun isChanged(actionSettings: RocketActionSettings): Boolean =
                                !(settings.id() == actionSettings.id() &&
                                    settings.settings() == actionSettings.settings())

                            override fun component(): Component = menu
                        }
                    }
            }

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String = "Выполнение скрипта Kotlin"

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, SCRIPT)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(key = SCRIPT, name = SCRIPT.value, description = "Скрипт для выполнения", required = true),
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Заголовок", required = true),
            createRocketActionProperty(key = DESCRIPTION, name = DESCRIPTION.value, description = "Описание", required = false),
            createRocketActionProperty(
                key = EXECUTE_ON_LOAD,
                name = EXECUTE_ON_LOAD.value,
                description = "Выполнять скрипт при загрузке",
                required = true,
                property = RocketActionPropertySpec.BooleanPropertySpec(defaultValue = false),
            ),
        )
    }

    override fun name(): String = "Kotlin script"

    override fun icon(): Icon? = ScriptMenu.ICON_DEFAULT

    companion object {
        internal const val TYPE = "KOTLIN_SCRIPT"
        internal val LABEL = RocketActionConfigurationPropertyKey("label")
        internal val SCRIPT = RocketActionConfigurationPropertyKey("script")
        internal val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        internal val EXECUTE_ON_LOAD = RocketActionConfigurationPropertyKey("executeOnLoad")
    }
}
