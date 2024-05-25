package ru.ezhov.rocket.action.application.search.infrastructure.regex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RegExSearchEngineTest {

    @Test
    fun `should register find and delete`() {
        val regExSearchEngine = RegExSearchEngine()

        regExSearchEngine.register("1", "Search MDM and Jira")
        regExSearchEngine.register("1", "Confluence")
        regExSearchEngine.register("1", "https://google.com")

        regExSearchEngine.register("2", "Документы для определения связей UML")
        regExSearchEngine.register("2", "MDM для всего")
        regExSearchEngine.register("2", "mdm для всего")
        regExSearchEngine.register("2", "https://github.com/")

        var result = regExSearchEngine.ids("MDM")
        assertThat(result).isEqualTo(listOf("1", "2"))

        result = regExSearchEngine.ids("flue")
        assertThat(result).isEqualTo(listOf("1"))

        result = regExSearchEngine.ids("://")
        assertThat(result).isEqualTo(listOf("1", "2"))

        result = regExSearchEngine.ids("MDM|mdm")
        assertThat(result).isEqualTo(listOf("1", "2"))

        regExSearchEngine.delete("1")
        regExSearchEngine.delete("2")
    }
}
