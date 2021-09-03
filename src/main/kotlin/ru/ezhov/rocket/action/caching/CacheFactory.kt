package ru.ezhov.rocket.action.caching

object CacheFactory {
    val cache: Cache = DiskCache()
}