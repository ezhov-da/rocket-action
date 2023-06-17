package ru.ezhov.rocket.action.application.tags.infrastructure

import ru.ezhov.rocket.action.application.tags.domain.Tag
import ru.ezhov.rocket.action.application.tags.domain.TagNode
import ru.ezhov.rocket.action.application.tags.domain.TagsRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryTagsRepository : TagsRepository {
    private val mapTagAndKeys: ConcurrentHashMap<String, MutableSet<String>> = ConcurrentHashMap()
    private val mapKeyAndTags: ConcurrentHashMap<String, List<String>> = ConcurrentHashMap()

    override fun replaceOrAdd(key: String, tags: List<String>) {
        val tagsFromMap = mapKeyAndTags[key]
        if (tagsFromMap != tags) {
            mapKeyAndTags[key] = tags

            tags.forEach { label ->
                val set = mapTagAndKeys[label] ?: mutableSetOf()
                set.remove(key)
                mapTagAndKeys[label] = set
            }

            add(key = key, tags = tags)
        }
    }

    private fun add(key: String, tags: List<String>) {
        tags.forEach { label ->
            val set = mapTagAndKeys[label] ?: mutableSetOf()
            set.add(key)
            mapTagAndKeys[label] = set
        }
    }

    override fun by(tag: String): Tag? =
        mapTagAndKeys[tag]
            ?.let { keys -> Tag(name = tag, keys = keys.toList()) }

    override fun tags(): List<String> = mapTagAndKeys.keys().toList()

    override fun tags(prefix: String): List<String> =
        mapTagAndKeys.keys().toList().filter { it.startsWith(prefix) }

    override fun tagAndKeys(): Map<String, Set<String>> =
        mapTagAndKeys.toMap()

    override fun countTags(): Int = tags().size

    override fun tagsTree(): List<TagNode> {
        val mapByIndex = mutableMapOf<Int, MutableList<TagNode.Builder>>()

        // we start running through the keys
        mapKeyAndTags.forEach { (key, tags) ->
            // for each tag in the key
            tags.forEachIndexed { index, tag ->
                // create a node
                val tagNode = TagNode.Builder(name = tag)
                tagNode.keys.add(key)

                // get all nodes of its level
                val list = mapByIndex.getOrPut(index) { mutableListOf() }
                if (list.isEmpty()) {
                    list.add(tagNode)
                } else {
                    val result = list.firstOrNull { it.name == tag }
                    if (result == null) {
                        list.add(tagNode)
                    } else {
                        result.keys.add(key)
                    }
                }

                if (index != 0) {
                    val listNodes = mapByIndex[index - 1]
                    val parent = listNodes?.firstOrNull { it.name == tags[index - 1] }

                    val alreadyUsage = parent?.children?.firstOrNull { it.name == tag }
                    if (alreadyUsage != null) {
                        alreadyUsage.keys.add(key)
                    } else {
                        parent?.children?.add(tagNode)
                    }
                }
            }
        }

        return mapByIndex[0]?.map { it.build() }?.toList().orEmpty()
    }
}
