package ru.ezhov.rocket.action.application.tags.ui

import ru.ezhov.rocket.action.application.tags.application.TagServiceFactory

object TagsPanelFactory {
    fun panel(
        tags: List<String> = emptyList(),
    ): TagsPanel = TagsPanel(
        tags = tags,
        availableTagsService = TagServiceFactory.tagsService
    )
}
