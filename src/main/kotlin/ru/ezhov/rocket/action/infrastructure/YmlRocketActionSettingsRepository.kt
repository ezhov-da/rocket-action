package ru.ezhov.rocket.action.infrastructure

import org.yaml.snakeyaml.Yaml
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepository
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.api.RocketActionSettings
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

class YmlRocketActionSettingsRepository(private val uri: URI) : RocketActionSettingsRepository {
    @Throws(RocketActionSettingsRepositoryException::class)
    override fun actions(): List<RocketActionSettings> {
        try {
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
                return actions
            }
        } catch (ex: Exception) {
            throw RocketActionSettingsRepositoryException("//TODO", ex)
        }
    }

    @Throws(RocketActionSettingsRepositoryException::class)
    override fun save(settings: List<RocketActionSettings>) {
        val file = File(uri!!.path)
        try {
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
        } catch (ex: Exception) {
            throw RocketActionSettingsRepositoryException("//TODO", ex)
        }
    }

    private fun recursiveSettings(settings: List<RocketActionSettings>?, actions: MutableList<Map<String?, Any?>>) {
        for (data in settings!!) {
            val `object`: MutableMap<String?, Any?> = LinkedHashMap()
            `object`[TYPE] = data.type()
            `object`[ID] = data.id()
            data.settings().forEach { (key: String?, value: String?) -> `object`[key] = value }
            val actionsOriginal = data.actions()
            if (!actionsOriginal!!.isEmpty()) {
                val actionsForWrite: MutableList<Map<String?, Any?>> = ArrayList()
                recursiveSettings(actionsOriginal, actionsForWrite)
                `object`[ACTIONS] = actionsForWrite
            }
            actions.add(`object`)
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
        @kotlin.jvm.JvmStatic
        @Throws(RocketActionSettingsRepositoryException::class)
        fun createAction(action: LinkedHashMap<String, Any>): RocketActionSettings {
            return create(
                    getOrGenerateId(action),
                    action[TYPE].toString(),
                    action
            )
        }

        @kotlin.jvm.JvmStatic
        @Throws(RocketActionSettingsRepositoryException::class)
        fun create(id: String, actionType: String, action: LinkedHashMap<String, Any>): RocketActionSettings {
            val actions = action[ACTIONS] as ArrayList<LinkedHashMap<String, Any>>?
            action.remove(TYPE)
            action.remove(ID)
            action.remove(ACTIONS)
            return if (actions == null || actions.isEmpty()) {
                val map: MutableMap<String, String> = TreeMap()
                action.forEach { (k: String, v: Any?) -> map[k] = v?.toString() ?: "" }
                MutableRocketActionSettings(id, actionType, map, mutableListOf())
            } else {
                val settings: MutableList<RocketActionSettings> = ArrayList()
                for (a in actions) {
                    settings.add(createAction(a))
                }
                val map: MutableMap<String, String> = TreeMap()
                action.forEach { (k: String, v: Any) -> map[k] = v.toString() }
                MutableRocketActionSettings(id, actionType, map, settings)
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