package ru.ezhov.rocket.action.plugin.note.ui

import ru.ezhov.rocket.action.api.context.RocketActionContext
import ru.ezhov.rocket.action.api.context.cache.CacheService
import ru.ezhov.rocket.action.api.context.icon.IconService
import ru.ezhov.rocket.action.api.context.notification.NotificationService
import ru.ezhov.rocket.action.plugin.note.application.NoteApplicationService
import ru.ezhov.rocket.action.plugin.note.infrastructure.NoteRepositoryFactory
import java.io.File

fun main() {
    NoteDialog(
        noteApplicationService = NoteApplicationService(
            NoteRepositoryFactory
                .sqLiteNoteRepository(File("db/test_notes.s3db"))
        ),
        actionContext = object : RocketActionContext {
            override fun icon(): IconService {
                TODO("Not yet implemented")
            }

            override fun notification(): NotificationService {
                TODO("Not yet implemented")
            }

            override fun cache(): CacheService {
                TODO("Not yet implemented")
            }

        }
    ).isVisible = true
}
