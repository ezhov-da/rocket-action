package ru.ezhov.rocket.action.application.tags.application

import ru.ezhov.rocket.action.application.tags.domain.TagNode
import ru.ezhov.rocket.action.application.tags.domain.TagsRepository

class TagsServiceImpl(
    private val tagsRepository: TagsRepository
) : TagsService {
    override fun add(key: String, tags: List<String>) {
        tagsRepository.replaceOrAdd(key, tags)
    }

    override fun tags(): List<TagDto> =
        tagsRepository
            .tags()
            .map {
                TagDto(name = it)
            }

    override fun tags(prefix: String): List<TagDto> =
        tagsRepository
            .tags(prefix)
            .map {
                TagDto(name = it)
            }

    override fun count(): Int = tagsRepository.countTags()

    override fun tagAndKeys(): List<TagAndKeys> =
        tagsRepository
            .tagAndKeys().map {
                TagAndKeys(
                    name = it.key,
                    keys = it.value
                )
            }
            .sortedBy { it.name }

    override fun tagsTree(): List<TagNode> = tagsRepository.tagsTree()
}
