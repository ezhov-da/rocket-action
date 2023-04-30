package ru.ezhov.rocket.action.application.tags.application

import ru.ezhov.rocket.action.application.tags.domain.TagNode

interface TagsService {
    fun add(key: String, tags: List<String>)

    fun tags(): List<TagDto>

    fun tags(prefix: String): List<TagDto>

    fun count(): Int

    fun tagAndKeys(): List<TagAndKeys>

    fun tagsTree(): List<TagNode>
}

data class TagDto(
    val name: String
)

data class TagAndKeys(
    val name: String,
    val keys: Set<String>,
)
