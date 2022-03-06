package ru.ezhov.rocket.action

import com.formdev.flatlaf.FlatLightLaf
import mu.KotlinLogging
import ru.ezhov.rocket.action.configuration.infrastructure.RocketActionConfigurationRepositoryFactory
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.infrastructure.RocketActionUiRepositoryFactory
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository
import ru.ezhov.rocket.action.properties.GeneralPropertiesRepositoryFactory
import ru.ezhov.rocket.action.properties.UsedPropertiesName
import java.io.File
import java.util.Enumeration
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    SwingUtilities.invokeLater {
        FlatLightLaf.setup()
        try {
            getFont()?.let { font -> setUIFont(font) }

            val actionService = UiQuickActionService(
                rocketActionSettingsRepository = rockerActionRepository(args),
                rocketActionConfigurationRepository = RocketActionConfigurationRepositoryFactory.repository,
                rocketActionUiRepository = RocketActionUiRepositoryFactory.repository,
                generalPropertiesRepository = GeneralPropertiesRepositoryFactory.repository
            )
            BaseDialog.dialog.apply {
                jMenuBar = actionService.createMenu(this)
                isUndecorated = true
                pack()
                setLocationRelativeTo(null)
                isAlwaysOnTop = true
                isVisible = true
            }
        } catch (e: UiQuickActionServiceException) {
            e.printStackTrace()
        }
    }
}

private fun rockerActionRepository(args: Array<String>): RocketActionSettingsRepository =
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
            repository.save(emptyList())
            logger.info { "File '${file.absolutePath}' with actions created" }
        }
        repository
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
