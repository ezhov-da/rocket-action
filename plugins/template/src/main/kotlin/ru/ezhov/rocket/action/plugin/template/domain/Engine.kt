package ru.ezhov.rocket.action.plugin.template.domain

interface Engine {
    fun apply(template: String, values: Map<String, String>): String
    fun words(text: String): List<String>
}