//package ru.ezhov.rocket.action.application
//
//import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
//import ru.ezhov.rocket.action.api.RocketActionSettings
//import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
//import ru.ezhov.rocket.action.application.infrastructure.YmlRocketActionSettingsRepository
//import java.io.File
//import java.io.StringReader
//import java.sql.Connection
//import java.sql.DriverManager
//import java.util.UUID
//
//fun main() {
//    val repository: RocketActionSettingsRepository =
//        YmlRocketActionSettingsRepository(File("./application/src/main/resources/test-actions.yml").toURI())
//    DriverManager.getConnection("jdbc:h2:./db/test/rocket-action/rocket-action", "rocket-action", "rocket-action")
//        .use { connection ->
//            val actions = repository.actions()
//            addActionToDb(actions = actions, connection = connection, parent = null)
//        }
//}
//
//private fun addActionToDb(actions: List<RocketActionSettings>, connection: Connection, parent: RocketActionSettings?) {
//    var order = 1
//    actions.forEach { ac ->
//        with(connection.prepareStatement("INSERT INTO ACTION(ID, \"TYPE\", \"SEQUENCE_ORDER\", PARENT_ID) VALUES (?, ?, ?, ?)")) {
//            setObject(1, UUID.fromString(ac.id()))
//            setString(2, ac.type().value())
//            setInt(3, order)
//            setObject(4, parent?.id()?.let { UUID.fromString(it) })
//
//            execute()
//        }
//        ac.settings().forEach { (key: RocketActionConfigurationPropertyKey, value: String) ->
//            with(connection.prepareStatement("INSERT INTO ACTION_SETTINGS(ID, \"NAME\", \"DATA\") VALUES (?, ?, ?)")) {
//                setObject(1, UUID.fromString(ac.id()))
//                setString(2, key.value)
//                setCharacterStream(3, StringReader(value))
//
//                execute()
//            }
//        }
//
//        order++
//
//        if (ac.actions().isNotEmpty()) {
//            addActionToDb(actions = ac.actions(), connection = connection, parent = ac)
//        }
//    }
//}
