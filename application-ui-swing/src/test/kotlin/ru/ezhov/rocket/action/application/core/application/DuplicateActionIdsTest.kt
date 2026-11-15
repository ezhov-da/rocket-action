package ru.ezhov.rocket.action.application.core.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel

internal class DuplicateActionIdsTest {
    @Test
    fun `should report a duplicated id once`() {
        val duplicates = duplicatesOf(
            listOf(
                action("same"),
                action("unique"),
                action("same"),
                parent("same"),
            )
        )

        assertThat(duplicates).containsExactly("same")
    }

    @Test
    fun `should not report anything for unique ids`() {
        assertThat(duplicatesOf(listOf(action("1"), action("2")))).isEmpty()
    }

    private fun action(id: String) = RocketActionSettingsModel(
        id = id,
        type = "TEST",
        settings = emptyList(),
        actions = emptyList(),
        tags = emptyList(),
    )

    private fun parent(id: String) = RocketActionSettingsModel(
        id = "parent",
        type = "GROUP",
        settings = emptyList(),
        actions = listOf(action(id)),
        tags = emptyList(),
    )
}
