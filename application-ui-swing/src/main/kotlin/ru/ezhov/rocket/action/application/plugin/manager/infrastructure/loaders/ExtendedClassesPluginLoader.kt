package ru.ezhov.rocket.action.application.plugin.manager.infrastructure.loaders

class ExtendedClassesPluginLoader {
    fun plugins(): List<String> =
        System.getProperty("rocket.action.extended.plugin.classess")
            ?.split(";")
            ?.map { it.trimIndent().trim() }
            ?: emptyList()
}
