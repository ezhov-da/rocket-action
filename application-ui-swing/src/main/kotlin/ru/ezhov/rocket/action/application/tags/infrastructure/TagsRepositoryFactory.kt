package ru.ezhov.rocket.action.application.tags.infrastructure

import ru.ezhov.rocket.action.application.tags.domain.TagsRepository

object TagsRepositoryFactory {
    private val repositoryInstance: TagsRepository = InMemoryTagsRepository()

    val repository: TagsRepository = repositoryInstance
}
