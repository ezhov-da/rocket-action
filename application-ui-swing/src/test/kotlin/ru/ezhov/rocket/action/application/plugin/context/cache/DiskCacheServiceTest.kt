package ru.ezhov.rocket.action.application.plugin.context.cache

import org.junit.Ignore
import org.junit.Test
import java.net.MalformedURLException
import java.net.URL

@Ignore
class DiskCacheServiceTest {
    @Test
    @Throws(MalformedURLException::class)
    fun test() {
        val cache = DiskCacheService()
        cache.get(URL("https://www.elastic.co/favicon.ico"))?.let {
            println(it)
        }
    }
}
