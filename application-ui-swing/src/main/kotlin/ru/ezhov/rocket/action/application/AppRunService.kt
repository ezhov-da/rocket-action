package ru.ezhov.rocket.action.application


import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.chainaction.startup.domain.StartupActionsRunStarter
import ru.ezhov.rocket.action.application.chainaction.startup.domain.model.StartupRunTrigger
import ru.ezhov.rocket.action.application.properties.GeneralPropertiesRepository
import ru.ezhov.rocket.action.application.properties.UsedPropertiesName
import java.util.*
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource
import kotlin.system.exitProcess

@Service
class AppRunService(
    private val generalPropertiesRepository: GeneralPropertiesRepository,
    private val baseDialogFactory: BaseDialogFactory,
    private val chainActionDialogFactory: ChainActionDialogFactory,
    private val startupActionsRunStarter: StartupActionsRunStarter,
) {

    /**
     * All UI must be created without Spring because LaF is not init in Spring
     */
    fun run(args: Array<String>) {
        try {
            getFont()?.let { font -> setUIFont(font) }

            baseDialogFactory.dialog.isVisible = true
            // Пока отключаем для тестирования // TODO ezhov
            //  chainActionDialogFactory.dialog?.isVisible = true

            // The main window is already visible, so long startup actions can not delay it.
            startupActionsRunStarter.startAsync(StartupRunTrigger.STARTUP)

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
}
