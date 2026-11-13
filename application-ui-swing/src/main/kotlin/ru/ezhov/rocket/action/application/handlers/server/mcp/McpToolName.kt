package ru.ezhov.rocket.action.application.handlers.server.mcp

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.chainaction.domain.model.Action

private val logger = KotlinLogging.logger {}

/**
 * Resolution of the name under which an action is published as an MCP tool.
 *
 * A custom name wins when it is not blank after normalization, otherwise the name is
 * generated from the action name and ID. Both the MCP server and the action dialogs use
 * this object, so the dialog validation and the published name cannot disagree.
 */
object McpToolName {
    private const val MAX_TOOL_NAME_LENGTH = 128
    private const val NAME_SEPARATOR = "__"
    private val INVALID_CHARACTERS = Regex("[^A-Za-z0-9_-]")

    /**
     * Name published for the action: the normalized custom name, or the generated one.
     */
    fun resolve(custom: String?, id: String, name: String): String =
        custom
            ?.let { normalize(it) }
            ?.takeIf { it.isNotBlank() }
            ?: generated(id, name)

    fun generated(id: String, name: String): String =
        normalize(name + NAME_SEPARATOR + id.replace("-", ""))

    /**
     * Transliterates the value, replaces characters not allowed in a tool name
     * and limits the result to 128 characters.
     */
    fun normalize(value: String): String =
        value.trim()
            .toTranslit()
            .replace(INVALID_CHARACTERS, "_")
            .take(MAX_TOOL_NAME_LENGTH)

    /**
     * First published action other than [exceptId] that would share the candidate name.
     */
    fun conflict(candidate: String, actions: List<Action>, exceptId: String?): Action? {
        val normalized = normalize(candidate)

        return actions.firstOrNull { action ->
            action.id() != exceptId &&
                resolve(action.mcpToolName(), action.id(), action.name()) == normalized
        }
    }

    /**
     * Keeps the first action for every resolved name and logs the skipped duplicates,
     * so a hand-edited file cannot break the MCP tool list.
     */
    fun published(actions: List<McpAction>): List<McpAction> {
        val names = mutableSetOf<String>()

        return actions.filter { action ->
            val added = names.add(action.toolName)
            if (!added) {
                logger.warn {
                    "MCP tool name '${action.toolName}' is already used, " +
                        "action '${action.action.id()}' is not published"
                }
            }
            added
        }
    }

    private fun String.toTranslit(): String {
        val charMap = mapOf(
            'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d",
            'е' to "e", 'ё' to "yo", 'ж' to "zh", 'з' to "z", 'и' to "i",
            'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m", 'н' to "n",
            'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t",
            'у' to "u", 'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch",
            'ш' to "sh", 'щ' to "sch", 'ъ' to "", 'ы' to "y", 'ь' to "",
            'э' to "e", 'ю' to "yu", 'я' to "ya",
            'А' to "A", 'Б' to "B", 'В' to "V", 'Г' to "G", 'Д' to "D",
            'Е' to "E", 'Ё' to "Yo", 'Ж' to "Zh", 'З' to "Z", 'И' to "I",
            'Й' to "Y", 'К' to "K", 'Л' to "L", 'М' to "M", 'Н' to "N",
            'О' to "O", 'П' to "P", 'Р' to "R", 'С' to "S", 'Т' to "T",
            'У' to "U", 'Ф' to "F", 'Х' to "H", 'Ц' to "Ts", 'Ч' to "Ch",
            'Ш' to "Sh", 'Щ' to "Sch", 'Ъ' to "", 'Ы' to "Y", 'Ь' to "",
            'Э' to "E", 'Ю' to "Yu", 'Я' to "Ya"
        )

        return buildString {
            for (char in this@toTranslit) {
                append(charMap[char] ?: char)
            }
        }
    }
}
