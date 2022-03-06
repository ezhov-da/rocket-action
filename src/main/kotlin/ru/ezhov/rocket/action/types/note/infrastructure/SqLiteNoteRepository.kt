package ru.ezhov.rocket.action.types.note.infrastructure

import com.mchange.v2.c3p0.ComboPooledDataSource
import mu.KotlinLogging
import ru.ezhov.rocket.action.types.note.domain.NoteRepository
import ru.ezhov.rocket.action.types.note.domain.model.Note
import java.io.File

private val logger = KotlinLogging.logger {}

class SqLiteNoteRepository(private val fileDb: File) : NoteRepository {

    companion object {
        private const val PROPERTY_TEXT = "text"
        private const val PROPERTY_DESCRIPTION = "description"
        private const val TAGS = "tags"
    }

    private val dataSource: ComboPooledDataSource = getDataSource()

    private fun getDataSource(): ComboPooledDataSource {
        Class.forName("org.sqlite.JDBC")
        val cpds = ComboPooledDataSource()
        cpds.jdbcUrl = "jdbc:sqlite:${fileDb.absolutePath.replace(oldValue = "\\", newValue = "/")}"
            .let {
                logger.debug { "SqlLite jdbcUrl '$it'" }; it
            }

        // Optional Settings
        cpds.initialPoolSize = 2
        cpds.minPoolSize = 1
        cpds.acquireIncrement = 3
        cpds.maxPoolSize = 3
        cpds.maxStatements = 20

        logger.debug { "SqlLite connection pool initialized" }

        return cpds
    }

    private fun createTable() {
        val query = """
            CREATE TABLE IF NOT EXISTS NOTES (
                ID TEXT NOT NULL,
                PROPERTY_NAME TEXT  NULL,
                PROPERTY_VALUE TEXT NULL,
                CREATION_TIME TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                val executeResult = statement.execute(query)
                logger.info { "Create table query execute result '$executeResult'" }
            }
        }
    }

    init {
        createTable()
    }

    override fun notes(): List<Note> {
        val selectQuery = """
            SELECT 
            ID, PROPERTY_NAME, PROPERTY_VALUE 
            FROM NOTES ORDER BY ID;
        """.trimIndent()
        val notes = mutableListOf<Note>()
        var mapValues = mutableMapOf<String, String>()
        var id: String? = null
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(selectQuery).use { rs ->
                    while (rs.next()) {
                        val currentId = rs.getString(1)
                        if (id == null) {
                            id = currentId
                        }
                        if (id != currentId) {
                            notes.add(
                                Note.of(
                                    id = id!!,
                                    text = mapValues[PROPERTY_TEXT],
                                    description = mapValues[PROPERTY_DESCRIPTION],
                                )
                            )

                            mapValues = mutableMapOf()
                        }
                        mapValues[rs.getString(2)] = rs.getString(3)
                    }
                }
            }
        }
        return notes
    }

    override fun create(note: Note) {
        val insertQuery = """
            INSERT INTO NOTES (ID, PROPERTY_NAME, PROPERTY_VALUE) VALUES(?,?,?);
        """.trimIndent()

        dataSource.connection.use { connection ->
            connection.prepareStatement(insertQuery).use { statement ->
                statement.setString(1, note.id)
                statement.setString(2, PROPERTY_TEXT)
                statement.setString(3, note.text)
                statement.addBatch()

                statement.setString(1, note.id)
                statement.setString(2, PROPERTY_DESCRIPTION)
                statement.setString(3, note.description)
                statement.addBatch()

                statement.executeBatch()
                logger.info { "Saved note $note" }
            }
        }
    }
}