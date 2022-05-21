//package ru.ezhov.rocket.action.application
//
//import com.formdev.flatlaf.FlatLightLaf
//import mu.KotlinLogging
//import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
//import ru.ezhov.rocket.action.application.plugin.manager.infrastructure.RocketActionPluginRepositoryFactory
//import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
//import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepositoryFactory
//import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
//import java.io.File
//import java.util.Enumeration
//import javax.swing.LookAndFeel
//import javax.swing.SwingUtilities
//import javax.swing.UIManager
//import javax.swing.plaf.FontUIResource
//
//private val logger = KotlinLogging.logger { }
//
//fun main(args: Array<String>) {
//    val generalPropertiesRepository = GeneralPropertiesRepositoryFactory.repository
//    SwingUtilities.invokeLater {
//        FlatLightLaf.setup(lookAndFeel(generalPropertiesRepository))
//        try {
//            getFont(generalPropertiesRepository)?.let { font -> setUIFont(font) }
//
//            val actionService = UiQuickActionService(
//                rocketActionSettingsRepository = rockerActionRepository(args),
//                rocketActionPluginRepository = RocketActionPluginRepositoryFactory.repository,
//                generalPropertiesRepository = generalPropertiesRepository,
//            )
//            BaseDialog.dialog.apply {
//                jMenuBar = actionService.createMenu(this)
//                isUndecorated = true
//                setLocationRelativeTo(null)
//                isAlwaysOnTop = true
//                pack()
//                isVisible = true
//            }
//        } catch (e: UiQuickActionServiceException) {
//            e.printStackTrace()
//        }
//    }
//}
//
//private fun rockerActionRepository(args: Array<String>): RocketActionSettingsRepository =
//    if (GeneralPropertiesRepositoryFactory.repository.asBoolean(UsedPropertiesName.IS_DEVELOPER, false)) {
//        val testActions = "/test-actions.yml"
//
//        logger.info { "Develop mode enabled. absolute path to `$testActions` file as argument" }
//
//        YmlRocketActionSettingsRepository(
//            uri = UiQuickActionService::class.java.getResource(testActions).toURI()
//        )
//    } else {
//        val path = if (args.isNotEmpty()) {
//            args[0]
//        } else {
//            GeneralPropertiesRepositoryFactory.repository.asString(
//                name = UsedPropertiesName.DEFAULT_ACTIONS_FILE,
//                default = "./actions.yml"
//            )
//        }
//        val file = File(path)
//        val repository = YmlRocketActionSettingsRepository(uri = file.toURI())
//        if (file.exists()) {
//            logger.info { "File '${file.absolutePath}' with actions exists" }
//        } else {
//            repository.save(emptyList())
//            logger.info { "File '${file.absolutePath}' with actions created" }
//        }
//        repository
//    }
//
//private fun lookAndFeel(repository: GeneralPropertiesRepository): LookAndFeel {
//    val className = repository
//        .asString(name = UsedPropertiesName.UI_CONFIGURATION_LOOK_AND_FEEL_CLASS,
//            default = "com.formdev.flatlaf.FlatLightLaf")
//
//    return Class.forName(className).newInstance() as LookAndFeel
//}
//
//private fun getFont(repository: GeneralPropertiesRepository): FontUIResource? {
//    val name = repository.asStringOrNull(UsedPropertiesName.FONT_NAME)
//    val style = repository.asIntegerOrNull(UsedPropertiesName.FONT_STYLE)
//    val size = repository.asIntegerOrNull(UsedPropertiesName.FONT_SIZE)
//
//    return if (name != null && style != null && size != null) {
//        FontUIResource(name, style, size)
//    } else {
//        null
//    }
//}
//
//private fun setUIFont(f: FontUIResource?) {
//    val keys: Enumeration<*> = UIManager.getDefaults().keys()
//    while (keys.hasMoreElements()) {
//        val key = keys.nextElement()
//        val value = UIManager.get(key)
//        if (value is FontUIResource) UIManager.put(key, f)
//    }
//}
