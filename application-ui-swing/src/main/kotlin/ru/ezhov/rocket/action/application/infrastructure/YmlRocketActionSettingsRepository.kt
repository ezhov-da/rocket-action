package ru.ezhov.rocket.action.application.infrastructure

import arrow.core.Either
import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepositoryException
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.UUID

private val logger = KotlinLogging.logger {}

class YmlRocketActionSettingsRepository(private val uri: URI) : RocketActionSettingsRepository {
    override fun actions(): List<RocketActionSettings> {
        logger.debug { "Get actions by uri='$uri'" }

        uri.toURL().openStream().use { inputStream ->
            val actions: MutableList<RocketActionSettings> = ArrayList()
            val yaml = Yaml()
            val obj = yaml.load<Map<String, Any>>(inputStream)
            for ((key, value) in obj) {
                if (ACTIONS == key) {
                    val linkedHashMaps = value as ArrayList<LinkedHashMap<String, Any>>
                    for (l in linkedHashMaps) {
                        actions.add(createAction(l))
                    }
                }
            }

            logger.info { "Actions count ${actions.size}" }

            return actions
        }
    }

    override fun save(settings: List<RocketActionSettings>) {
        logger.debug { "Actions settings saving started. count=${settings.size}" }

        val file = File(uri.path)
        OutputStreamWriter(
            FileOutputStream(file),
            StandardCharsets.UTF_8).use { outputStreamWriter ->
            val yaml = Yaml()
            val all: MutableList<Map<String?, Any?>> = ArrayList()
            recursiveSettings(settings, all)
            val map: MutableMap<String, Any> = LinkedHashMap()
            map[ACTIONS] = all
            yaml.dump(map, outputStreamWriter)
        }

        logger.info { "Actions settings saving completed. count=${settings.size}" }
    }

    override fun save(settings: RocketActionSettings): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun create(settings: RocketActionSettings): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun delete(id: String): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun before(id: String, beforeId: String): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    override fun after(id: String, afterId: String): Either<RocketActionSettingsRepositoryException, Unit> {
        TODO("Not yet implemented")
    }

    private fun recursiveSettings(settings: List<RocketActionSettings>?, actions: MutableList<Map<String?, Any?>>) {
        for (data in settings!!) {
            val objectYml: MutableMap<String?, Any?> = LinkedHashMap()
            objectYml[TYPE] = data.type().value()
            objectYml[ID] = data.id()
            data.settings().forEach { (key: RocketActionConfigurationPropertyKey?, value: String?) -> objectYml[key.value] = value }
            val actionsOriginal = data.actions()
            if (actionsOriginal.isNotEmpty()) {
                val actionsForWrite: MutableList<Map<String?, Any?>> = ArrayList()
                recursiveSettings(actionsOriginal, actionsForWrite)
                objectYml[ACTIONS] = actionsForWrite
            }
            actions.add(objectYml)
        }
    }

    @Throws(RocketActionSettingsRepositoryException::class)
    private fun createAction(action: LinkedHashMap<String, Any>): RocketActionSettings {
        return QuickActionFactory.create(
            getOrGenerateId(action),
            action[TYPE].toString(),
            action
        )
    }

    private object QuickActionFactory {
        @Throws(RocketActionSettingsRepositoryException::class)
        fun createAction(action: LinkedHashMap<String, Any>): RocketActionSettings {
            return create(
                getOrGenerateId(action),
                action[TYPE].toString(),
                action
            )
        }

        @Throws(RocketActionSettingsRepositoryException::class)
        fun create(id: String, actionType: String, action: LinkedHashMap<String, Any>): RocketActionSettings {
            val actions = action[ACTIONS] as ArrayList<LinkedHashMap<String, Any>>?
            action.remove(TYPE)
            action.remove(ID)
            action.remove(ACTIONS)
            return if (actions == null || actions.isEmpty()) {
                val map: MutableMap<RocketActionConfigurationPropertyKey, String> = HashMap()
                action.forEach { (k: String, v: Any?) -> map[RocketActionConfigurationPropertyKey(k)] = v?.toString().orEmpty() }
                RocketActionSettingsNode(id, { actionType }, map, mutableListOf())
            } else {
                val settings: MutableList<RocketActionSettings> = ArrayList()
                for (a in actions) {
                    settings.add(createAction(a))
                }
                val map: MutableMap<RocketActionConfigurationPropertyKey, String> = HashMap()
                action.forEach { (k: String, v: Any) -> map[RocketActionConfigurationPropertyKey(k)] = v.toString() }
                RocketActionSettingsNode(id, { actionType }, map, settings)
            }
        }
    }

    companion object {
        private const val TYPE = "type"
        private const val ID = "_id"
        private const val ACTIONS = "actions"
        private fun getOrGenerateId(action: LinkedHashMap<String, Any>): String {
            var idAsObject = action[ID]
            if (idAsObject == null || "" == idAsObject.toString()) {
                idAsObject = UUID.randomUUID().toString()
            }
            return idAsObject.toString()
        }
    }
}
