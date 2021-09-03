package ru.ezhov.rocket.action.types.todoist

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.types.todoist.TodoistRepositoryException

@Ignore
class TodoistTaskRepositoryTest {
    @Test
    @Throws(TodoistRepositoryException::class)
    fun shouldGetAllProjects() {
        val repository = TodoistTaskRepository()
        val tasks = repository.tasks(stubRocketActionSettings())
        Assert.assertFalse(tasks.isEmpty())
    }

    private fun stubRocketActionSettings(): RocketActionSettings {
        return object : RocketActionSettings {
            override fun id(): String  = ""

            override fun type(): String = ""

            override fun settings(): Map<String, String> = emptyMap()

            override fun actions(): List<RocketActionSettings>  = emptyList()
        }
    }
}