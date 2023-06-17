package ru.ezhov.rocket.action.plugin.script.dynamic.ui

import ru.ezhov.rocket.action.api.RocketAction
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
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
        Executing a script with input parameters.
        Each parameter is a variable '_v[field number]'.
        For example: _v1 - field 1
        """.trimIndent()

    override fun asString(): List<String> = listOf(LABEL, SCRIPT)

    override fun properties(): List<RocketActionConfigurationProperty> {
        return listOf(
            createRocketActionProperty(
                key = SCRIPT,
                name = SCRIPT,
                description = "Script to execute",
                required = true
            ),
            createRocketActionProperty(key = LABEL, name = LABEL, description = "Title", required = true),
            createRocketActionProperty(
                key = DESCRIPTION,
                name = DESCRIPTION,
                description = "Description",
                required = false
            ),
            createRocketActionProperty(
                key = FIELD_NAMES,
                name = FIELD_NAMES,
                description = """
                    Field names and default values, if necessary, with a ':' delimiter in the column.
                    For example:
                    Name: Denis
                    Query: select * from test where name = 'Denis'
                """.trimIndent(),
                required = false
            ),
            createRocketActionProperty(
                key = COUNT_VARIABLES,
                name = COUNT_VARIABLES,
                description = "Number of input parameters",
                required = true,
                property = RocketActionPropertySpec.IntPropertySpec(defaultValue = 1, min = 0, max = 10),
            ),
            createRocketActionProperty(
                key = SELECTED_SCRIPT_LANG,
                name = SELECTED_SCRIPT_LANG,
                description = "Selected script language",
                required = true,
                property = RocketActionPropertySpec.ListPropertySpec(
                    ScriptEngineType.GROOVY.name,
                    ScriptEngineType.values().map { it.name }
                ),
            )
        )
    }

    override fun name(): String = "Script with variables (alfa)"

    override fun icon(): Icon = actionContext!!.icon().by(AppIcon.BOLT)

    companion object {
        internal const val TYPE = "DYNAMIC_SCRIPT"
        internal const val LABEL = "label"
        internal const val SCRIPT = "script"
        internal const val DESCRIPTION = "description"
        internal const val COUNT_VARIABLES = "countVariables"
        internal const val SELECTED_SCRIPT_LANG = "selectedScriptLang"
        internal const val FIELD_NAMES = "fieldNames"
    }
}
