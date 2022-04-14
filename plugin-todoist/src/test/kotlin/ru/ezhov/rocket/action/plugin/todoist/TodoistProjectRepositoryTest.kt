package ru.ezhov.rocket.action.plugin.todoist

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

@Ignore
class TodoistProjectRepositoryTest {
    @Test
    fun shouldGetAllProjects() {
        val repository = TodoistProjectRepository()
        val projects = repository.projects("token")
        assertThat(projects.isEmpty()).isFalse
    }
}
