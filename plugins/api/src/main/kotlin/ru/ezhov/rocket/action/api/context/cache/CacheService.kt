package ru.ezhov.rocket.action.api.context.cache

import java.io.File
import java.net.URL

interface CacheService {
    fun get(url: URL): File?
}
