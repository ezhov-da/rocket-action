package ru.ezhov.rocket.action.application.plugin.context.icon

import com.mortennobel.imagescaling.AdvancedResizeOp
import com.mortennobel.imagescaling.ResampleOp
import mu.KotlinLogging
import net.sf.image4j.codec.ico.ICODecoder
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.api.context.notification.NotificationType
import ru.ezhov.rocket.action.application.core.application.ActionReloadScope
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.SwingUtilities

private val logger = KotlinLogging.logger { }

/**
 * Loads action icons without blocking the menu reload.
 *
 * Decoded icons are cached for the application lifetime (keyed by URL) and failed URLs are
 * remembered for ten minutes so an unreachable host is not retried on every reload.
 */
@Service
class ResourceLoaderService(
    private val cacheService: CacheService,
    private val notificationService: NotificationService,
    private val fetchExecutor: ExecutorService = defaultFetchExecutor(),
) {
    private val icons = ConcurrentHashMap<String, Icon>()
    private val failedUntilNanos = ConcurrentHashMap<String, Long>()
    private val inFlight = ConcurrentHashMap.newKeySet<String>()

    fun load(iconUrl: String, defaultIcon: Icon): Icon = load(iconUrl, defaultIcon, null)

    /**
     * Returns immediately: a memory hit or a locally available file is decoded inline, anything
     * else is fetched by a background thread and delivered to [onLoaded] on the EDT.
     */
    fun load(iconUrl: String, defaultIcon: Icon, onLoaded: ((Icon) -> Unit)?): Icon {
        if (iconUrl.isEmpty()) {
            return defaultIcon
        }

        val scope = ActionReloadScope.currentOrNull()

        icons[iconUrl]?.let { icon ->
            scope?.iconHit()
            return icon
        }

        scope?.iconMiss()

        if (isFailed(iconUrl)) {
            return defaultIcon
        }

        val localFile = try {
            cacheService.getIfPresent(URL(iconUrl))
        } catch (ex: Exception) {
            logger.debug(ex) { "Error when check cached icon url='$iconUrl'" }
            null
        }

        if (localFile != null) {
            decode(localFile, iconUrl)?.let { icon ->
                icons[iconUrl] = icon
                return icon
            }
        }

        fetchInBackground(iconUrl = iconUrl, onLoaded = onLoaded)
        return defaultIcon
    }

    private fun fetchInBackground(iconUrl: String, onLoaded: ((Icon) -> Unit)?) {
        if (!inFlight.add(iconUrl)) {
            return
        }

        try {
            fetchExecutor.execute {
                try {
                    val file = cacheService.get(URL(iconUrl))
                    val icon = file?.let { decode(it, iconUrl) }
                    if (icon == null) {
                        markFailed(iconUrl, null)
                        return@execute
                    }

                    icons[iconUrl] = icon
                    onLoaded?.let { callback ->
                        SwingUtilities.invokeLater {
                            try {
                                callback(icon)
                            } catch (ex: Exception) {
                                logger.warn(ex) { "Error when apply loaded icon url='$iconUrl'" }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    markFailed(iconUrl, ex)
                } finally {
                    inFlight.remove(iconUrl)
                }
            }
        } catch (ex: Exception) {
            inFlight.remove(iconUrl)
            logger.warn(ex) { "Error when schedule icon loading url='$iconUrl'" }
        }
    }

    private fun decode(file: File, iconUrl: String): Icon? =
        try {
            val image: BufferedImage? =
                if (iconUrl.endsWith("ico")) {
                    val bufferedImages = ICODecoder.readExt(file)
                    bufferedImages[bufferedImages.size - 1].image
                } else {
                    ImageIO.read(file)
                }
            image?.let { ImageIcon(scaleImage(it)) }
        } catch (ex: Exception) {
            logger.warn(ex) { "Exception when load icon url='$iconUrl'" }
            null
        }

    private fun isFailed(iconUrl: String): Boolean {
        val until = failedUntilNanos[iconUrl] ?: return false
        if (until > System.nanoTime()) {
            return true
        }

        failedUntilNanos.remove(iconUrl)
        return false
    }

    private fun markFailed(iconUrl: String, cause: Exception?) {
        failedUntilNanos[iconUrl] = System.nanoTime() + TimeUnit.MINUTES.toNanos(FAILED_TTL_MINUTES)
        if (cause != null) {
            logger.warn(cause) { "Exception when load icon url='$iconUrl'" }
        } else {
            logger.warn { "Icon url='$iconUrl' is not available" }
        }

        try {
            notificationService.show(NotificationType.ERROR, "Error icon loading")
        } catch (ex: Exception) {
            logger.debug(ex) { "Error when notify about icon loading failure" }
        }
    }

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val resampleOp = ResampleOp(16, 16)
        resampleOp.unsharpenMask = AdvancedResizeOp.UnsharpenMask.Oversharpened
        return resampleOp.filter(image, null)
    }

    companion object {
        private const val FAILED_TTL_MINUTES = 10L

        fun defaultFetchExecutor(): ExecutorService =
            Executors.newFixedThreadPool(2) { runnable ->
                Thread(runnable, "rocket-action-icon").apply { isDaemon = true }
            }
    }
}
