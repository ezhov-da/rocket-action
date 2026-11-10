package ru.ezhov.rocket.action.plugin.note.domain

import ru.ezhov.rocket.action.plugin.note.domain.model.Note

interface NoteRepository {
    fun notes(): List<Note>

    fun create(note: Note)
}