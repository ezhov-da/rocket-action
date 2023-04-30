package ru.ezhov.rocket.action.application.tags.application

import ru.ezhov.rocket.action.application.tags.infrastructure.TagsRepositoryFactory

object TagServiceFactory {
    private val tagsServiceInstance: TagsService =
        TagsServiceImpl(TagsRepositoryFactory.repository)

    val tagsService = tagsServiceInstance
}
