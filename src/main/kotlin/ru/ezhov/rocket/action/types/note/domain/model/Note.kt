package ru.ezhov.rocket.action.types.note.domain.model

import java.util.UUID

data class Note private constructor(
    val id: String,
    val text: String?,
    val description: String?
) {
    companion object {
        fun of(
            id: String,
            text: String? = null,
            description: String? = null
        ) = Note(id = id, text = text, description = description)

        fun create(
            text: String?,
            description: String?
        ) = Note(id = UUID.randomUUID().toString(), text = text, description = description)
    }
}