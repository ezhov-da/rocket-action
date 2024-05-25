package ru.ezhov.rocket.action.plugin.config

import java.io.InputStream

object ConfigReaderFactory {
    fun yml(inputStream: InputStream): ConfigReader = YmlConfigReader(inputStream)
}
