package ru.ezhov.rocket.action.cache

import java.io.File
import java.net.URL

interface Cache {
    fun get(url: URL): File?
}