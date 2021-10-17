package ru.ezhov.rocket.action.types.note.ui

import ru.ezhov.rocket.action.types.note.application.NoteApplicationService
import ru.ezhov.rocket.action.types.note.infrastructure.NoteRepositoryFactory
import java.io.File
import javax.swing.JDialog

fun main() {
    NoteDialog(
            noteApplicationService = NoteApplicationService(
                    NoteRepositoryFactory
                            .sqLiteNoteRepository(File("db/test_notes.s3db"))
            ),
            owner = JDialog()
    ).isVisible = true
}
