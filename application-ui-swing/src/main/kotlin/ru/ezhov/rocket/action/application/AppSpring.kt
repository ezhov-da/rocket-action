package ru.ezhov.rocket.action.application

import com.formdev.flatlaf.FlatLightLaf
import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import ru.ezhov.rocket.action.application.config.AppConfigCoreSpring
import ru.ezhov.rocket.action.application.domain.ConfigRocketActionSettingsRepository
import ru.ezhov.rocket.action.application.plugin.manager.domain.RocketActionPluginRepository
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.util.Enumeration
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    val appContext: ApplicationContext = AnnotationConfigApplicationContext(AppConfigCoreSpring::class.java)
    val baseDialog = appContext.getBean(BaseDialogClass::class.java)
    val rocketActionPluginRepository = appContext.getBean(RocketActionPluginRepository::class.java)
    val generalPropertiesRepository = appContext.getBean(GeneralPropertiesRepository::class.java)
    val rocketActionSettingsRepository = appContext.getBean(ConfigRocketActionSettingsRepository::class.java)

    SwingUtilities.invokeLater {
        FlatLightLaf.setup(lookAndFeel(generalPropertiesRepository))
        try {
            getFont(generalPropertiesRepository)?.let { font -> setUIFont(font) }

            val actionService = UiQuickActionService(
                rocketActionSettingsRepository = rocketActionSettingsRepository,
                rocketActionPluginRepository = rocketActionPluginRepository,
                generalPropertiesRepository = generalPropertiesRepository
            )
            baseDialog.dialog.apply {
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

private fun lookAndFeel(repository: GeneralPropertiesRepository): LookAndFeel {
    val className = repository
        .asString(name = UsedPropertiesName.UI_CONFIGURATION_LOOK_AND_FEEL_CLASS,
            default = "com.formdev.flatlaf.FlatLightLaf")

    return Class.forName(className).newInstance() as LookAndFeel
}

private fun getFont(repository: GeneralPropertiesRepository): FontUIResource? {
    val name = repository.asStringOrNull(UsedPropertiesName.FONT_NAME)
    val style = repository.asIntegerOrNull(UsedPropertiesName.FONT_STYLE)
    val size = repository.asIntegerOrNull(UsedPropertiesName.FONT_SIZE)

    return if (name != null && style != null && size != null) {
        FontUIResource(name, style, size)
    } else {
        null
    }
}

private fun setUIFont(f: FontUIResource?) {
    val keys: Enumeration<*> = UIManager.getDefaults().keys()
    while (keys.hasMoreElements()) {
        val key = keys.nextElement()
        val value = UIManager.get(key)
        if (value is FontUIResource) UIManager.put(key, f)
    }
}
