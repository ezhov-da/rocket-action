package ru.ezhov.rocket.action.application.plugin.context.icon

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.Icon

internal class ResourceLoaderServiceCacheTest {
    private val defaultIcon: Icon = ImageIcon(BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB))

    @Test
    fun `should load and decode the same URL only once`() {
        val file = tempPng()
        val cacheService = CountingCacheService(file)
        val service = ResourceLoaderService(
            cacheService = cacheService,
            notificationService = mockk(relaxed = true),
            fetchExecutor = DirectExecutorService(),
        )

        // the first call starts the background fetch and returns the default icon
        val first = service.load("https://example.test/icon.png", defaultIcon)
        // the decoded icon is already in the application-lifetime cache for the next call
        val second = service.load("https://example.test/icon.png", defaultIcon)
        val third = service.load("https://example.test/icon.png", defaultIcon)

        assertThat(first).isSameAs(defaultIcon)
        assertThat(second).isNotSameAs(defaultIcon)
        assertThat(third).isSameAs(second)
        assertThat(cacheService.getIfPresentCalls).isEqualTo(1)
        assertThat(cacheService.getCalls).isEqualTo(1)
    }

    @Test
    fun `should return the default icon without waiting for an unreachable URL`() {
        val cacheService = CountingCacheService(file = null)
        val executor = QueueExecutorService()
        val service = ResourceLoaderService(
            cacheService = cacheService,
            notificationService = mockk(relaxed = true),
            fetchExecutor = executor,
        )

        val startedAt = System.nanoTime()
        val icon = service.load("http://10.255.255.1/unreachable.png", defaultIcon)
        val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)

        assertThat(icon).isSameAs(defaultIcon)
        assertThat(elapsedMs).isLessThan(500)
        assertThat(executor.queued()).hasSize(1)
        // the network read is deferred to the background task
        assertThat(cacheService.getCalls).isZero()
    }

    private fun tempPng(): File {
        val file = File.createTempFile("rocket-action-icon", ".png").apply { deleteOnExit() }
        ImageIO.write(BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), "png", file)
        return file
    }

    private class CountingCacheService(private val file: File?) : CacheService {
        var getCalls = 0
        var getIfPresentCalls = 0

        override fun get(url: URL): File? {
            getCalls++
            return file
        }

        override fun getIfPresent(url: URL): File? {
            getIfPresentCalls++
            return null
        }
    }

    private class DirectExecutorService : AbstractExecutorService() {
        private var shutdown = false

        override fun execute(command: Runnable) = command.run()
        override fun shutdown() {
            shutdown = true
        }

        override fun shutdownNow(): MutableList<Runnable> {
            shutdown = true
            return mutableListOf()
        }

        override fun isShutdown(): Boolean = shutdown
        override fun isTerminated(): Boolean = shutdown
        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = true
    }

    private class QueueExecutorService : AbstractExecutorService() {
        private val tasks = mutableListOf<Runnable>()

        fun queued(): List<Runnable> = tasks.toList()

        override fun execute(command: Runnable) {
            tasks.add(command)
        }

        override fun shutdown() = Unit
        override fun shutdownNow(): MutableList<Runnable> = tasks.toMutableList()
        override fun isShutdown(): Boolean = false
        override fun isTerminated(): Boolean = false
        override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean = true
    }
}
