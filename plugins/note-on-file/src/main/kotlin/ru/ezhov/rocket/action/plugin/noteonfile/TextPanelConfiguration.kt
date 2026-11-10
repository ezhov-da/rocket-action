package ru.ezhov.rocket.action.plugin.noteonfile

data class TextPanelConfiguration(
    val path: String,
    val label: String,
    val style: String?,
    val loadOnInitialize: Boolean,
    val addStyleSelected: Boolean,
    val delimiter: String,
)
