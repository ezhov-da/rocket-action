package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionFactoryUi
import ru.ezhov.rocket.action.api.RocketActionPlugin
import ru.ezhov.rocket.action.api.RocketActionPropertySpec
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.support.AbstractRocketAction
import ru.ezhov.rocket.action.plugin.script.ScriptEngineType
import javax.swing.Icon

class DynamicScriptRocketActionUi : AbstractRocketAction(), RocketActionPlugin {
    private var actionContext: RocketActionContext? = null

    override fun factory(context: RocketActionContext): RocketActionFactoryUi = this
        .apply { actionContext = context }

    override fun configuration(context: RocketActionContext): RocketActionConfiguration = this
        .apply { actionContext = context }

    override fun create(settings: RocketActionSettings, context: RocketActionContext): RocketAction? =
        UiService().build(settings, context)

    override fun type(): RocketActionType = RocketActionType { TYPE }

    override fun description(): String =
        """
        Выполнение скрипта с входными параметрами.
        Каждый параметр является переменной '_v[номер поля]'.
        Например: _v1 - поле 1
        """.trimIndent()

    override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(LABEL, SCRIPT)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = SCRIPT,
                name = SCRIPT.value,
                description = "Скрипт для выполнения",
                required = true
            ),
            createRocketActionProperty(key = LABEL, name = LABEL.value, description = "Заголовок", required = true),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION.value,
                description = "Описание",
                required = false
            ),
            createRocketActionProperty(
                key = FIELD_NAMES,
                name = FIELD_NAMES.value,
                description = """
                    Имена полей и значения по умолчанию, если необходимо с разделителем ':' в столбец.
                    Например:
                    Имя:Денис
                    Запрос:select * from test where name = 'Денис'
                """.trimIndent(),
                required = false
            ),
            createRocketActionProperty(
                key = COUNT_VARIABLES,
                name = COUNT_VARIABLES.value,
                description = "Количество входных параметров",
                required = true,
                property = RocketActionPropertySpec.IntPropertySpec(defaultValue = 1, min = 0, max = 10),
            ),
            createRocketActionProperty(
                key = SELECTED_SCRIPT_LANG,
                name = SELECTED_SCRIPT_LANG.value,
                description = "Выбранный язык скрипта",
                required = true,
                property = RocketActionPropertySpec.ListPropertySpec(
                    ScriptEngineType.GROOVY.name,
                    ScriptEngineType.values().map { it.name }
                ),
            )
        )
    }

    override fun name(): String = "Скрипт с переменными (alfa)"

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.BOLT)

    companion object {
        internal const val TYPE = "DYNAMIC_SCRIPT"
        internal val LABEL = RocketActionConfigurationPropertyKey("label")
        internal val SCRIPT = RocketActionConfigurationPropertyKey("script")
        internal val DESCRIPTION = RocketActionConfigurationPropertyKey("description")
        internal val COUNT_VARIABLES = RocketActionConfigurationPropertyKey("countVariables")
        internal val SELECTED_SCRIPT_LANG = RocketActionConfigurationPropertyKey("selectedScriptLang")
        internal val FIELD_NAMES = RocketActionConfigurationPropertyKey("fieldNames")
    }
}
