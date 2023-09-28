package ru.ezhov.rocket.action.application

import com.formdev.flatlaf.FlatLightLaf
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.core.application.RocketActionSettingsService
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.infrastructure.yml.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.handlers.server.Server
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationService
import ru.ezhov.rocket.action.application.plugin.manager.application.RocketActionPluginApplicationServiceFactory
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import ru.ezhov.rocket.action.application.tags.application.TagServiceFactory
import java.io.File
import java.util.*
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        FlatLightLaf.setup(lookAndFeel())
        try {
            getFont()?.let { font -> setUIFont(font) }

            val rocketActionPluginApplicationService = RocketActionPluginApplicationServiceFactory.service
            val actionService = UiQuickActionService(
                rocketActionSettingsService = rockerActionService(args, rocketActionPluginApplicationService),
                rocketActionPluginApplicationService = rocketActionPluginApplicationService,
                generalPropertiesRepository = GeneralPropertiesRepositoryFactory.repository,
                tagsService = TagServiceFactory.tagsService,
            )

            runServer() // TODO decorate beautifully

            BaseDialog.dialog.apply {
                jMenuBar = actionService.createMenu(this)
                isUndecorated = true
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                pack()
                isVisible = true
            }
        } catch (e: UiQuickActionServiceException) {
            e.printStackTrace()
        }
    }
}

private fun rockerActionService(
    args: Array<String>,
    rocketActionPluginApplicationService: RocketActionPluginApplicationService
): RocketActionSettingsService =
    if (GeneralPropertiesRepositoryFactory.repository.asBoolean(UsedPropertiesName.IS_DEVELOPER, false)) {
        val testActions = "/test-actions.yml"

        logger.info { "Develop mode enabled. absolute path to `$testActions` file as argument" }

        YmlRocketActionSettingsRepository(
            uri = UiQuickActionService::class.java.getResource(testActions).toURI()
        )
    } else {
        val path = if (args.isNotEmpty()) {
            args[0]
        } else {
            GeneralPropertiesRepositoryFactory.repository.asString(
                name = UsedPropertiesName.DEFAULT_ACTIONS_FILE,
                default = "./actions.yml"
            )
        }
        val file = File(path)
        val repository = YmlRocketActionSettingsRepository(uri = file.toURI())
        if (file.exists()) {
            logger.info { "File '${file.absolutePath}' with actions exists" }
        } else {
            repository.save(ActionsModel(actions = emptyList()))
            logger.info { "File '${file.absolutePath}' with actions created" }
        }
        repository
    }
        .let { repository ->
            repository.load()
            RocketActionSettingsService(
                rocketActionPluginApplicationService = rocketActionPluginApplicationService,
                rocketActionSettingsRepository = repository,
            )
        }

private fun lookAndFeel(): LookAndFeel {
    val className = GeneralPropertiesRepositoryFactory.repository
        .asString(
            name = UsedPropertiesName.UI_CONFIGURATION_LOOK_AND_FEEL_CLASS,
            default = "com.formdev.flatlaf.FlatLightLaf"
        )

    return Class.forName(className).newInstance() as LookAndFeel
}

private fun getFont(): FontUIResource? {
    val name = GeneralPropertiesRepositoryFactory.repository.asStringOrNull(UsedPropertiesName.FONT_NAME)
    val style = GeneralPropertiesRepositoryFactory.repository.asIntegerOrNull(UsedPropertiesName.FONT_STYLE)
    val size = GeneralPropertiesRepositoryFactory.repository.asIntegerOrNull(UsedPropertiesName.FONT_SIZE)

    return if (name != null && style != null && size != null) {
        FontUIResource(name, style, size)
    } else {
        null
    }
}

fun setUIFont(f: FontUIResource?) {
    val keys: Enumeration<*> = UIManager.getDefaults().keys()
    while (keys.hasMoreElements()) {
        val key = keys.nextElement()
        val value = UIManager.get(key)
        if (value is FontUIResource) UIManager.put(key, f)
    }
}

private fun runServer() {
    Server().run()
}
