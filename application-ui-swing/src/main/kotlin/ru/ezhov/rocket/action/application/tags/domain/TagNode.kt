package ru.ezhov.rocket.action.application.tags.domain

@Suppress("DataClassPrivateConstructor")
data class TagNode private constructor(
    val name: String,
    val children: List<TagNode>,
    val keys: Set<String> = mutableSetOf()
) {
    class Builder(
        val name: String,
        val children: MutableList<Builder> = mutableListOf(),
        val keys: MutableSet<String> = mutableSetOf()
    ) {
        fun build(): TagNode = TagNode(
            name = name,
            children = children.map { it.build() },
            keys = keys
        )
    }
}
