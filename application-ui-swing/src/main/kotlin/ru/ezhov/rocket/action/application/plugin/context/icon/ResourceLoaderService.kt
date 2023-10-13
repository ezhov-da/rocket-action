package ru.ezhov.rocket.action.application.plugin.context.icon

import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleOp
import mu.KotlinLogging
import net.sf.image4j.codec.ico.ICODecoder
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

private val logger = KotlinLogging.logger { }

@Service
class ResourceLoaderService(
    private val cacheService: CacheService,
    private val notificationService: NotificationService,
) {
    fun load(iconUrl: String, defaultIcon: Icon): Icon =
        iconUrl
            .takeIf { it.isNotEmpty() }
            ?.let { url ->
                try {
                    cacheService.get(URL(url))
                        ?.let { file ->
                            val image: BufferedImage =
                                if (url.endsWith("ico")) {
                                    val bufferedImages = ICODecoder.readExt(file)
                                    bufferedImages[bufferedImages.size - 1].image
                                } else {
                                    ImageIO.read(file)
                                }
                            ImageIcon(scaleImage(image))
                        }
                } catch (e: Exception) {
                    logger.warn(e) { "Exception when load icon url='$iconUrl'" }
                    notificationService.show(NotificationType.ERROR, "Error icon loading")
                    defaultIcon
                }
            } ?: defaultIcon

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val resampleOp = ResampleOp(16, 16)
        resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Oversharpened
        return resampleOp.filter(image, null)
    }
}
