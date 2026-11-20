package ru.ezhov.rocket.action.application.core.application

import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.plugin.group.GroupRocketActionUi

/**
 * Where the last reload placed every action in the menu.
 *
 * The reload keeps one cached Swing component per action id, and a component has exactly one
 * parent. Showing search results therefore re-parents the found components into the content
 * menu and physically disassembles the groups. This snapshot is the source of the structure,
 * so a restore can put the components back even when the groups were reused and not rebuilt.
 *
 * It holds ids only: no model, no evaluated settings.
 */
internal data class MenuStructure(
    /** Top-level ids in catalog order. */
    val roots: List<String>,
    /** Ids whose component owns its children (only [GroupRocketActionUi.TYPE]). */
    val containers: Set<String>,
    /** Ordered child ids of every container. */
    val children: Map<String, List<String>>,
) {
    companion object {
        fun of(actions: List<RocketActionSettingsModel>): MenuStructure {
            val containers = linkedSetOf<String>()
            val children = linkedMapOf<String, List<String>>()

            fun visit(level: List<RocketActionSettingsModel>) {
                level.forEach { action ->
                    if (action.type == GroupRocketActionUi.TYPE) {
                        containers.add(action.id)
                        if (!children.containsKey(action.id)) {
                            // duplicate ids are logged by the reload; the first placement wins,
                            // as in the assembly that consumes this snapshot
                            children[action.id] = action.actions.map { it.id }
                        }
                    }
                    visit(action.actions)
                }
            }

            visit(actions)

            return MenuStructure(
                roots = actions.map { it.id },
                containers = containers,
                children = children,
            )
        }
    }
}
