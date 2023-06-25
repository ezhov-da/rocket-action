package ru.ezhov.rocket.action.plugin.noteonfile

import io.mockk.mockk
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.context.search.Search
import java.io.File
import java.net.URL
import java.nio.file.Files
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.io.path.absolutePathString

fun main() {
    SwingUtilities.invokeLater {
        JFrame()
            .apply {
                val textPanelConfiguration = TextPanelConfiguration(
                    path = Files.createTempFile("123", "111").absolutePathString(),
                    label = "Test",
                    loadOnInitialize = true,
                    style = null,
                    addStyleSelected = true,
                    delimiter = "",
                )
                add(
                    TextPanel(
                        textPanelConfiguration = textPanelConfiguration,
                        textAutoSave = TextAutoSave(enable = true, delayInSeconds = 3),
                        context = object : RocketActionContext {
                            override fun icon(): IconService =
                                object : IconService {
                                    override fun by(icon: AppIcon): Icon = ImageIcon()

                                    override fun load(iconUrl: String, defaultIcon: Icon): Icon = defaultIcon

                                }

                            override fun notification(): NotificationService =
                                object : NotificationService {
                                    override fun show(type: NotificationType, text: String) {
                                        println("$type - $text")
                                    }

                                }

                            override fun cache(): CacheService =
                                object : CacheService {
                                    override fun get(url: URL): File? = null
                                }

                            override fun search(): Search = mockk()
                        }

                    )
                )

                pack()
                defaultCloseOperation = JFrame.EXIT_ON_CLOSE
                isVisible = true
            }
    }
}
