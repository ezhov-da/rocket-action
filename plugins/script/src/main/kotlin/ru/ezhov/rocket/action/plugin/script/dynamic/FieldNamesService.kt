package ru.ezhov.rocket.action.plugin.script.dynamic

class FieldNamesService {
    fun get(value: String, separator: String): List<FieldName> =
        value
            .split("\n")
            .map { r ->
                FieldName(
                    name = r.substringBefore(separator),
                    value = r.substringAfter(separator, missingDelimiterValue = "")
                )
            }
}

data class FieldName(
    val name: String,
    val value: String,
)
