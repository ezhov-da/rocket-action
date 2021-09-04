package ru.ezhov.rocket.action.caching

import java.io.File
import java.net.URL

interface Cache {
    fun get(url: URL): File?
}