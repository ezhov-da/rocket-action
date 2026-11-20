package ru.ezhov.rocket.action.application.core.application

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.core.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi
import java.lang.reflect.Modifier

internal class MenuStructureTest {
    @Test
    fun `should keep ids and catalog order of nested groups`() {
        val structure = MenuStructure.of(
            listOf(
                group("g1", leaf("a"), group("g2", leaf("b"), group("g3", leaf("c")))),
                leaf("d"),
            )
        )

        assertThat(structure.roots).containsExactly("g1", "d")
        assertThat(structure.containers).containsExactly("g1", "g2", "g3")
        assertThat(structure.children["g1"]).containsExactly("a", "g2")
        assertThat(structure.children["g2"]).containsExactly("b", "g3")
        assertThat(structure.children["g3"]).containsExactly("c")
    }

    @Test
    fun `should keep ids only and not retain the model`() {
        val structure = MenuStructure.of(listOf(group("g1", leaf("a"))))

        val propertyTypes = MenuStructure::class.java.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .map { it.genericType.typeName }

        assertThat(propertyTypes).containsExactlyInAnyOrder(
            "java.util.List<java.lang.String>",
            "java.util.Set<java.lang.String>",
            "java.util.Map<java.lang.String, java.util.List<java.lang.String>>",
        )
        assertThat(structure.children).containsOnlyKeys("g1")
    }

    private fun group(id: String, vararg children: RocketActionSettingsModel) =
        RocketActionSettingsModel(
            id = id,
            type = GroupRocketActionUi.TYPE,
            settings = listOf(SettingsModel(name = "label", value = id)),
            actions = children.toList(),
            tags = emptyList(),
        )

    private fun leaf(id: String) = RocketActionSettingsModel(
        id = id,
        type = "TEST",
        settings = emptyList(),
        actions = emptyList(),
        tags = emptyList(),
    )
}
