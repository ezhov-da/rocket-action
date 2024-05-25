package ru.ezhov.rocket.action.application.tags.ui.create

import ru.ezhov.rocket.action.application.tags.application.TagsService

object TagsPanelFactory {
    fun panel(
        tags: List<String> = emptyList(),
        tagsService: TagsService,
    ): TagsPanel = TagsPanel(
        tags = tags,
        tagsService = tagsService
    )
}
