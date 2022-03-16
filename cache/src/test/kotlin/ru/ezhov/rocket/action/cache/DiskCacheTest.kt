package ru.ezhov.rocket.action.cache

import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

class DiskCacheTest {
    @Test
    @Throws(MalformedURLException::class)
    fun test() {
        val cache = DiskCache()
        cache.get(URL("https://www.elastic.co/favicon.ico"))?.let {
            println(it)
        }
    }
}