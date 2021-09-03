package ru.ezhov.rocket.action.types.todoist

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.types.todoist.TodoistRepositoryException

@Ignore
class TodoistProjectRepositoryTest {
    @Test
    @Throws(TodoistRepositoryException::class)
    fun shouldGetAllProjects() {
        val repository = TodoistProjectRepository()
        val projects = repository.projects(object : RocketActionSettings {
            override fun id(): String = ""

            override fun type(): String = ""

            override fun settings(): Map<String, String> = emptyMap()

            override fun actions(): List<RocketActionSettings> = emptyList()
        })
        Assert.assertFalse(projects.isEmpty())
    }
}