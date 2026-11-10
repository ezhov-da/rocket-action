package ru.ezhov.rocket.action.plugin.note.infrastructure

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.plugin.note.domain.model.Note
import java.io.File

@Disabled
class SqLiteNoteRepositoryTest {
    @Test
    fun testSelectData() {
        val repo = SqLiteNoteRepository(File("test_notes.s3db"))

        println(repo.notes())
    }

    @Test
    fun testCreate() {
        val repo = SqLiteNoteRepository(File("test_notes.s3db"))

        repo.create(Note.create("1", "12"))
    }
}
