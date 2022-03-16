package ru.ezhov.rocket.action.cache

object CacheFactory {
    val cache: Cache = DiskCache()
}