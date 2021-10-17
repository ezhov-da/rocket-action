package ru.ezhov.rocket.action.types.note.infrastructure

import ru.ezhov.rocket.action.types.note.domain.NoteRepository
import java.io.File


object NoteRepositoryFactory {
    private var sqLiteNoteRepository: SqLiteNoteRepository? = null

    fun sqLiteNoteRepository(dbFile: File): NoteRepository {
        if (sqLiteNoteRepository == null) {
            sqLiteNoteRepository = SqLiteNoteRepository(dbFile)
        }
        return sqLiteNoteRepository!!
    }
}