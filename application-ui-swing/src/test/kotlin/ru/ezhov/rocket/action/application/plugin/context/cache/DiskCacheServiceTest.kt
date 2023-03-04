package ru.ezhov.rocket.action.application.plugin.context.cache

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.URL

@Disabled
class DiskCacheServiceTest {
    @Test
    fun test() {
        val cache = DiskCacheService()
        cache.get(URL("https://www.elastic.co/favicon.ico"))?.let {
            println(it)
        }
    }
}
