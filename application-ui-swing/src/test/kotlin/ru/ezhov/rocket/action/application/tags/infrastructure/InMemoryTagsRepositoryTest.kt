package ru.ezhov.rocket.action.application.tags.infrastructure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.tags.domain.TagNode

internal class InMemoryTagsRepositoryTest {
    @Test
    fun `should be success when work with tags`() {
        val repository = InMemoryTagsRepository()
        repository.replaceOrAdd(key = "1", tags = listOf("test", "another", "very_good"))
        repository.replaceOrAdd(key = "2", tags = listOf("test", "another2", "very_good2"))

        assertThat(repository.tags()).hasSize(5)
        assertThat(repository.by("test")!!.keys).isEqualTo(listOf("1", "2"))
        assertThat(repository.tags("ver")).isEqualTo(listOf("very_good2", "very_good"))
    }

    @Test
    fun `should create tree tags`() {
        val repository = InMemoryTagsRepository()
        repository.replaceOrAdd(key = "1", tags = listOf("test", "another", "very_good"))
        repository.replaceOrAdd(key = "2", tags = listOf("test", " another2", " very_good2"))

        val nodes = repository.tagsTree()

        assertThat(nodes).isEqualTo(
            listOf(
                TagNode.Builder(
                    name = "test",
                    children = mutableListOf(
                        TagNode.Builder(
                            name = "another",
                            children = mutableListOf(
                                TagNode.Builder(
                                    name = "very_good",
                                    children = mutableListOf(),
                                    keys = mutableSetOf("1")
                                )
                            ),
                            keys = mutableSetOf("1")
                        ),
                        TagNode.Builder(
                            name = "another2",
                            children = mutableListOf(
                                TagNode.Builder(
                                    name = "very_good2",
                                    children = mutableListOf(),
                                    keys = mutableSetOf("2")
                                )
                            ),
                            keys = mutableSetOf("2")
                        ),
                    ),
                    keys = mutableSetOf("1", "2")
                )
            )
        )
    }
}
