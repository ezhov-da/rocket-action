package ru.ezhov.rocket.action.plugin.file.ui

import io.mockk.mockk
import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.api.context.search.Search
import ru.ezhov.rocket.action.plugin.file.domain.TemporaryFileService
import java.io.File
import java.net.URL
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.SwingUtilities

object TemporaryFileUiTest {
    @JvmStatic
    fun main(args: Array<String>) {
        SwingUtilities.invokeLater {
            JFrame().apply {
                add(TemporaryFileUi(
                    temporaryFileService = TemporaryFileService(),
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
                    }))

                pack()
                isVisible = true
            }
        }
    }
}
