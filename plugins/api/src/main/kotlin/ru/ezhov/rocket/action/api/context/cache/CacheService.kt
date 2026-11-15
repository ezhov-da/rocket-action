package ru.ezhov.rocket.action.api.context.cache

import java.io.File
import java.net.URL

interface CacheService {
    fun get(url: URL): File?

    /**
     * Returns the cached file only when it is already available locally, without any network access.
     * The default implementation reports "not available" so custom caches stay compatible.
     */
    fun getIfPresent(url: URL): File? = null
}
