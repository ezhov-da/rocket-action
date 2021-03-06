package ru.ezhov.rocket.action.icon

import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleOp
import mu.KotlinLogging
import net.sf.image4j.codec.ico.ICODecoder
import ru.ezhov.rocket.action.cache.CacheFactory
import ru.ezhov.rocket.action.notification.NotificationFactory
import ru.ezhov.rocket.action.notification.NotificationType
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

private val logger = KotlinLogging.logger { }

class IconService {
    fun load(iconUrl: String, defaultIcon: Icon): Icon =
        iconUrl
            .takeIf { it.isNotEmpty() }
            ?.let { url ->
                try {
                    CacheFactory.cache.get(URL(url))
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
                    NotificationFactory.notification.show(NotificationType.ERROR, "Error icon loading")
                    defaultIcon
                }
            } ?: defaultIcon

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val resampleOp = ResampleOp(16, 16)
        resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Oversharpened
        return resampleOp.filter(image, null)
    }
}