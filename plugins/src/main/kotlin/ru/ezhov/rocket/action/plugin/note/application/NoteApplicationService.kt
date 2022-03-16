package ru.ezhov.rocket.action.plugin.note.application

import ru.ezhov.rocket.action.plugin.note.domain.NoteRepository
import ru.ezhov.rocket.action.plugin.note.domain.model.Note

class NoteApplicationService(private val noteRepository: NoteRepository) {
    fun create(text: String, description: String): Note {
        val note = Note.create(text, description)
        noteRepository.create(note)
        return note
    }
}