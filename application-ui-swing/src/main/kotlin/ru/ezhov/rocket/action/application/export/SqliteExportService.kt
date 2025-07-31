package ru.ezhov.rocket.action.application.export

import mu.KotlinLogging
import ru.ezhov.rocket.action.application.core.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.core.domain.model.RocketActionSettingsModel
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

private val logger = KotlinLogging.logger { }

class SqliteExportService(
    private val pathToDb: File
) : RocketActionExporter {
    private val dropTableSqlQuery = """
        DROP TABLE IF EXISTS "ROCKET_ACTION";
    """.trimIndent()

    private val createTableSqlQuery = """
        CREATE TABLE IF NOT EXISTS "ROCKET_ACTION" (
        	"ID"	TEXT NOT NULL,
        	"PARENT_ID"	TEXT,
        	"TYPE" TEXT NOT NULL,
        	"SETTING_NAME"	TEXT NOT NULL,
        	"SETTING_VALUE"	TEXT NOT NULL,
        	"SETTING_VALUE_TYPE"	TEXT
        );
    """.trimIndent()

    private val insertDataSqlQuery = """
        INSERT INTO "ROCKET_ACTION" (
            "ID",
            "PARENT_ID",
            "TYPE",
            "SETTING_NAME",
            "SETTING_VALUE",
            "SETTING_VALUE_TYPE"
        ) VALUES (
            ?,
            ?,
            ?,
            ?,
            ?,
            ?
        )
    """.trimIndent()

    override fun export(actions: ActionsModel) {
        val list = mutableListOf<InsertAction>()
        prepareActions(parentId = null, actions = actions.actions, mutableList = list)
        insert(list, getConnection(file()))
    }

    private fun file(): File =
        if (pathToDb.isDirectory) {
            val newFile = File(pathToDb, "rocket-actions.db")
            if (newFile.exists()) {
                File(pathToDb, "rocket-actions-${System.currentTimeMillis()}.db")
            } else {
                newFile
            }
        } else {
            pathToDb
        }

    private fun getConnection(file: File): Connection {
        Class.forName("org.sqlite.JDBC")
        return DriverManager
            .getConnection("jdbc:sqlite:${file.absolutePath.replace(oldValue = "\\", newValue = "/")}")
    }

    private fun insert(actions: List<InsertAction>, connection: Connection) {
        connection.use { con ->
            con.autoCommit = false
            try {
                con.createStatement().use { st ->
                    st.execute(dropTableSqlQuery)
                    st.execute(createTableSqlQuery)
                }

                con.prepareStatement(insertDataSqlQuery).use { ps ->
                    actions.forEach { a ->
                        ps.setString(1, a.id)
                        ps.setString(2, a.parentId)
                        ps.setString(3, a.type)
                        ps.setString(4, a.settingName)
                        ps.setString(5, a.settingValue)
                        ps.setString(6, a.settingValueType)

                        ps.addBatch()
                    }
                    ps.executeBatch()
                }

                con.commit()
            } catch (ex: Exception) {
                try {
                    logger.error(ex) { "Transaction is being rolled back" }
                    con.rollback()
                } catch (e: SQLException) {
                    logger.error(e) { "Error when transaction is being rolled back" }
                }
                throw ex
            }
        }
    }

    private fun prepareActions(parentId: String?, actions: List<RocketActionSettingsModel>, mutableList: MutableList<InsertAction>) {
        actions.forEach { a ->
            a.settings.forEach { s ->
                mutableList.add(
                    InsertAction(
                        id = a.id,
                        type = a.type,
                        parentId = parentId,
                        settingName = s.name,
                        settingValue = s.value,
                        settingValueType = s.valueType?.name
                    )
                )
            }

            if (a.actions.isNotEmpty()) {
                prepareActions(parentId = a.id, actions = a.actions, mutableList = mutableList)
            }
        }
    }
}

private data class InsertAction(
    val id: String,
    val type: String,
    val parentId: String?,
    val settingName: String,
    val settingValue: String,
    val settingValueType: String?,
)
