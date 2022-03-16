package ru.ezhov.rocket.action.plugin.note.infrastructure

import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.plugin.note.domain.model.Note
import java.io.File

@Ignore
class SqLiteNoteRepositoryTest {
    @Test
    fun testSelectData() {
        val repo = SqLiteNoteRepository(File("db/test_notes.s3db"))

        println(repo.notes())
    }

    @Test
    fun testCreate() {
        val repo = SqLiteNoteRepository(File("db/test_notes.s3db"))

        repo.create(Note.create("1", "12"))
    }
}