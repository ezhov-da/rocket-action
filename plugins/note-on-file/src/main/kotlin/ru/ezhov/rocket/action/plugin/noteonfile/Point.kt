package ru.ezhov.rocket.action.plugin.noteonfile

data class Point private constructor(val index: Int, val text: String) {
    companion object {
        private const val MAX_TEXT_LENGTH = 50

        fun of(index: Int, text: String) = Point(
            index = index,
            text = text.takeIf { it.length > MAX_TEXT_LENGTH }?.substring(0, MAX_TEXT_LENGTH) ?: text
        )
    }
}
