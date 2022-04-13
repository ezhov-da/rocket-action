package ru.ezhov.rocket.action.plugin.note.ui

import ru.ezhov.rocket.action.plugin.note.application.NoteApplicationService
import ru.ezhov.rocket.action.plugin.note.infrastructure.NoteRepositoryFactory
import java.io.File

fun main() {
    NoteDialog(
        noteApplicationService = NoteApplicationService(
            NoteRepositoryFactory
                .sqLiteNoteRepository(File("db/test_notes.s3db"))
        ),
    ).isVisible = true
}
