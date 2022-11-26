package ru.ezhov.rocket.action.application.plugin.context.icon

import mu.KotlinLogging
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import javax.swing.Icon

class ResourceIconService : IconService {
    private val resourceIconRepository = ResourceIconRepository()
    private val resourceLoaderService = ResourceLoaderService()

    override fun by(icon: AppIcon): Icon = resourceIconRepository.by(icon)


    override fun load(iconUrl: String, defaultIcon: Icon): Icon =
        resourceLoaderService.load(iconUrl, defaultIcon)
}

