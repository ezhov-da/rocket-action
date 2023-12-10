package ru.ezhov.rocket.action.application


import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.handlers.server.HttpServer
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.util.*
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import kotlin.system.exitProcess

@Service
class AppRunService(
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val httpServer: HttpServer,
    private val baseDialogFactory: BaseDialogFactory,
) {

    /**
     * All UI must be created without Spring because LaF is not init in Spring
     */
    fun run(args: Array<String>) {
        runServer() // TODO decorate beautifully

        try {
            getFont()?.let { font -> setUIFont(font) }

            baseDialogFactory.dialog.isVisible = true

        } catch (e: UiQuickActionServiceException) {
            e.printStackTrace()
            exitProcess(-1)
        }
    }

    private fun getFont(): FontUIResource? {
        val name = generalPropertiesRepository.asStringOrNull(UsedPropertiesName.FONT_NAME)
        val style = generalPropertiesRepository.asIntegerOrNull(UsedPropertiesName.FONT_STYLE)
        val size = generalPropertiesRepository.asIntegerOrNull(UsedPropertiesName.FONT_SIZE)

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

    private fun runServer() {
        httpServer.run()
    }
}
