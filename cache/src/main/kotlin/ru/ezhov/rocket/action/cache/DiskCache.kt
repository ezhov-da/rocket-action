package ru.ezhov.rocket.action.cache

import com.google.common.hash.Hashing
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

class DiskCache : Cache {
    private val cacheFolder = File("./cache"        )

    override fun get(url: URL): File? {
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs()
        }
        checkQuietly()
        val sha256hexUrl = Hashing
            .sha256()
            .hashString(url.toString(), StandardCharsets.UTF_8)
            .toString()
        val file = File(cacheFolder, sha256hexUrl)
        return if (file.exists()) {
            file
        } else {
            readFromUrl(url, file)
        }
    }

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