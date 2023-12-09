package ru.ezhov.rocket.action.application.plugin.context.cache

import com.google.common.hash.Hashing
import mu.KotlinLogging
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.cache.CacheService
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

private val logger = KotlinLogging.logger {}

@Service
class DiskCacheService : CacheService {
    private val cacheFolder = File("./.rocket-action/cache")
    private val cache: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    override fun get(url: URL): File? {
        if (!cacheFolder.exists()) {
            val result = cacheFolder.mkdirs()
            logger.debug { "Cache folder='$cacheFolder' created with result='$result'" }
        } else {
            logger.debug { "Cache folder='$cacheFolder' already exists" }
        }
        checkQuietly()

        val sha256hexUrl = getHashFrom(url.toString())

        logger.debug { "SHA256='$sha256hexUrl' for url='$url'" }

        val file = File(cacheFolder, sha256hexUrl)
        return if (file.exists()) {
            logger.debug { "Return url='$url' from cache file='${file.absolutePath}'" }
            file
        } else {
            logger.debug { "Cache file does not exists. Load file from url='$url'" }
            readFromUrl(url, file)
        }
    }

    private fun getHashFrom(url: String) =
        cache[url]
            .apply {
                logger.debug { "Hash '$this' get from cache by URL '$url'" }
            }
            ?: run {
                createHashFrom(url).also { hash ->
                    cache[url] = hash
                    logger.debug { "Hash '$hash' created for URL '$url'" }
                }
            }

    private fun createHashFrom(url: String): String =
        Hashing
            .sha256()
            .hashString(url, StandardCharsets.UTF_8)
            .toString()

    private fun readFromUrl(url: URL, file: File): File? = try {
        url.openStream().use { inputStream ->
            FileOutputStream(file).use { fileOutputStream ->
                val buf = ByteArray(256)
                var p: Int
                while (inputStream.read(buf).also { p = it } != -1) {
                    fileOutputStream.write(buf, 0, p)
                }
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    companion object {
        fun checkQuietly() {
            try {
                HttpsURLConnection
                    .setDefaultHostnameVerifier { _: String?, _: SSLSession? -> true }
                val context = SSLContext.getInstance("TLS")
                context.init(
                    null,
                    arrayOf<X509TrustManager>(object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    }),
                    SecureRandom()
                )
                HttpsURLConnection.setDefaultSSLSocketFactory(context.socketFactory)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
