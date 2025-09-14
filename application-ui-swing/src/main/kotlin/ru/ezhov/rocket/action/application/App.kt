package ru.ezhov.rocket.action.application

import com.formdev.flatlaf.FlatLightLaf
import mu.KotlinLogging
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger { }

fun main(args: Array<String>) {
    // TODO ezhov
    //  We temporarily launch everything in this thread so that all forms are initialized with the same L&F
    SwingUtilities.invokeLater {
        try {
            // It is necessary to initialize L&F before working with the context,
            // otherwise some elements will not pick up the new L&F
            FlatLightLaf.setup(lookAndFeel())

            CommandLineArgsSingleton.args = args
            val appRunService: AppRunService = ApplicationContextFactory.context().getBean(AppRunService::class.java)
            appRunService.run(args)
        } catch (ex: Throwable) {
            logger.error(ex) { "Error when run application" }
            exitProcess(-1)
        }
    }
}

private fun lookAndFeel(): LookAndFeel {
    val className = System.getProperty(
        UsedPropertiesName.UI_CONFIGURATION_LOOK_AND_FEEL_CLASS.propertyName,
        "com.formdev.flatlaf.FlatLightLaf"
    )

    return Class.forName(className).newInstance() as LookAndFeel
}
