package ru.ezhov.rocket.action.types.note.domain

import ru.ezhov.rocket.action.types.note.domain.model.Note

interface NoteRepository {
    fun notes(): List<Note>

    fun create(note: Note)
}