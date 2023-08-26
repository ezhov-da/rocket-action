package ru.ezhov.rocket.action.plugin.jira.worklog.domain.model

@Suppress("DataClassPrivateConstructor")
data class AliasForTaskIds private constructor(
    val values: Map<String, List<String>> = emptyMap()
) {
    companion object {
        val EMPTY = AliasForTaskIds()

        fun of(aliases: String?): AliasForTaskIds {
            val v = aliases
                ?.let { aliasesNotNull ->
                    aliasesNotNull
                        .trim()
                        .split("\n")
                        .map { row ->
                            row
                                .split("_")
                        }
                        .mapNotNull {
                            if (it.size == 2) {
                                it[0] to it[1].split(",").map { v -> v.trim() }
                            } else {
                                null
                            }
                        }
                        .toMap()
                }
                ?: emptyMap()

            return AliasForTaskIds(v)
        }
    }

    fun taskIdByAlias(alias: String): String? =
        values.filterValues { it.contains(alias) }.keys.firstOrNull()

    fun aliasesBy(id: String): List<String> = values[id].orEmpty()
}
