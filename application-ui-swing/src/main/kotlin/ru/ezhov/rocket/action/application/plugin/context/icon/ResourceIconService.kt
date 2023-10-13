package ru.ezhov.rocket.action.application.plugin.context.icon

import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.api.context.icon.AppIcon
import ru.ezhov.rocket.action.api.context.icon.IconService
import javax.swing.Icon

@Service
class ResourceIconService(
    private val resourceIconRepository: ResourceIconRepository,
    private val resourceLoaderService: ResourceLoaderService,
) : IconService {

    override fun by(icon: AppIcon): Icon = resourceIconRepository.by(icon)

    override fun load(iconUrl: String, defaultIcon: Icon): Icon =
        resourceLoaderService.load(iconUrl, defaultIcon)
}

