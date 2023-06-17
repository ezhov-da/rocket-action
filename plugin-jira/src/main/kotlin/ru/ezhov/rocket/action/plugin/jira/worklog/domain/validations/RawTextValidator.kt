package ru.ezhov.rocket.action.plugin.jira.worklog.domain.validations

import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

class RawTextValidator(
    inputRules: String
) : Validator {
    companion object{
        const val EMPTY_RULES = ""
    }

    private val rules: Map<ValidationRule, Any> = parseRules(inputRules)

    override fun validate(source: String): List<String> =

        buildList {
            rules.forEach { (k, v) ->
                when (k) {
                    ValidationRule.MIN_LENGTH -> if (source.length < v as Int) {
                        add("The minimum length is '$v' characters. Now '${source.length}'")
                    }

                    ValidationRule.MAX_LENGTH -> if (source.length > v as Int) {
                        add("Maximum length of '$v' characters. Now'${source.length}'")
                    }
                }
            }
        }

    private fun parseRules(inputRules: String): Map<ValidationRule, Any> =
        inputRules
            .split("\n")
            .map { it.trim().split(" ") }
            .mapNotNull { rowOfRule ->
                if (rowOfRule.isNotEmpty() && rowOfRule.size == 2) {
                    val rule = rowOfRule.first().uppercase()
                    ValidationRule
                        .values()
                        .firstOrNull { it.name == rule }
                        ?.let { validationRule ->
                            val valueOfRule = when (validationRule) {
                                ValidationRule.MIN_LENGTH -> rowOfRule[1].toIntOrNull()
                                ValidationRule.MAX_LENGTH -> rowOfRule[1].toIntOrNull()
                            }

                            if (valueOfRule != null) {
                                Pair(validationRule, valueOfRule)
                            } else {
                                logger.debug { "Validation rules not build from source '$inputRules'" }
                                null
                            }
                        }
                } else null
            }
            .toMap()
}
