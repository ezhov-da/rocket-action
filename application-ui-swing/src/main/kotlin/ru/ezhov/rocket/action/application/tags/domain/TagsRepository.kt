package ru.ezhov.rocket.action.application.tags.domain

interface TagsRepository {
    fun replaceOrAdd(key: String, tags: List<String>)

    fun by(tag: String): Tag?

    fun tags(): List<String>

    fun tagAndKeys(): Map<String, Set<String>>

    fun tags(prefix: String): List<String>

    fun countTags(): Int

    fun tagsTree(): List<TagNode>

    fun clear()
}
