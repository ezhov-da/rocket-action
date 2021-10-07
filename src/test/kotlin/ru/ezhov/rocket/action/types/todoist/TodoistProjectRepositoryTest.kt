package ru.ezhov.rocket.action.types.todoist

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.types.todoist.TodoistRepositoryException

@Ignore
class TodoistProjectRepositoryTest {
    @Test
    @Throws(TodoistRepositoryException::class)
    fun shouldGetAllProjects() {
        val repository = TodoistProjectRepository()
        val projects = repository.projects("token")
        Assert.assertFalse(projects.isEmpty())
    }
}