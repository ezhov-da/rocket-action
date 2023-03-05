package ru.ezhov.rocket.action.application.infrastructure.yml

import mu.KotlinLogging
import org.yaml.snakeyaml.Yaml
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.application.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

private val logger = KotlinLogging.logger {}

@Deprecated(
    "Устаревший метод, который будет выводиться из обращения. " +
        "Используется только для обратной соместимости"
)
class YmlRocketActionSettingsRepositoryOldFormat(private val uri: URI) {
    fun actions(): List<RocketActionSettings> {
        logger.debug { "Get actions by uri='$uri'" }

        uri.toURL().openStream().use { inputStream ->
            val actions: MutableList<MutableRocketActionSettings> = ArrayList()
            val yaml = Yaml()
            val obj = yaml.load<Map<String, Any>>(inputStream)
            for ((key, value) in obj) {
                if (ACTIONS == key) {
                    val linkedHashMaps = value as ArrayList<LinkedHashMap<String, Any?>>
                    for (l in linkedHashMaps) {
                        actions.add(createAction(l))
                    }
                }
            }

            logger.info { "Actions count ${actions.size}" }

            return actions.map { it.to() }
        }
    }

    fun save(settings: List<RocketActionSettings>) {
        logger.debug { "Actions settings saving started. count=${settings.size}" }

        val file = File(uri.path)
        OutputStreamWriter(
            FileOutputStream(file),
            StandardCharsets.UTF_8
        ).use { outputStreamWriter ->
            val yaml = Yaml()
            val all: MutableList<Map<String?, Any?>> = ArrayList()
            recursiveSettings(settings, all)
            val map: MutableMap<String, Any> = LinkedHashMap()
            map[ACTIONS] = all
            yaml.dump(map, outputStreamWriter)
        }

        logger.info { "Actions settings saving completed. count=${settings.size}" }
    }

    private fun recursiveSettings(settings: List<RocketActionSettings>?, actions: MutableList<Map<String?, Any?>>) {
        for (data in settings!!) {
            val objectYml: MutableMap<String?, Any?> = LinkedHashMap()
            objectYml[TYPE] = data.type().value()
            objectYml[ID] = data.id()
            data.settings().forEach { (key: RocketActionConfigurationPropertyKey?, value: String?) ->
                objectYml[key.value] = value
            }
            val actionsOriginal = data.actions()
            if (actionsOriginal.isNotEmpty()) {
                val actionsForWrite: MutableList<Map<String?, Any?>> = ArrayList()
                recursiveSettings(actionsOriginal, actionsForWrite)
                objectYml[ACTIONS] = actionsForWrite
            }
            actions.add(objectYml)
        }
    }

    private fun createAction(action: LinkedHashMap<String, Any?>): MutableRocketActionSettings {
        return QuickActionFactory.create(
            getOrGenerateId(action),
            action[TYPE].toString(),
            action
        )
    }

    private object QuickActionFactory {
        fun createAction(action: LinkedHashMap<String, Any?>): MutableRocketActionSettings {
            return create(
                getOrGenerateId(action),
                action[TYPE].toString(),
                action
            )
        }

        fun create(id: String, actionType: String, action: LinkedHashMap<String, Any?>): MutableRocketActionSettings {
            val actions = (action[ACTIONS] as? ArrayList<LinkedHashMap<String, Any?>>).orEmpty()
            action.remove(TYPE)
            action.remove(ID)
            action.remove(ACTIONS)
            return if (actions.isEmpty()) {
                val list: MutableList<SettingsModel> = mutableListOf()
                action.forEach { (k: String, v: Any?) ->
                    list.add(
                        SettingsModel(
                            name = k,
                            value = v?.toString().orEmpty(),
                            valueType = null,

                            )
                    )
                }
                MutableRocketActionSettings(id = id, type = actionType, settings = list, actions = mutableListOf())
            } else {
                val settings: MutableList<MutableRocketActionSettings> = ArrayList()
                for (a in actions) {
                    settings.add(createAction(a))
                }
                val list: MutableList<SettingsModel> = mutableListOf()
                action.forEach { (k: String, v: Any?) ->
                    list.add(
                        SettingsModel(
                            name = k,
                            value = v?.toString().orEmpty(),
                            valueType = null,
                        )
                    )
                }
                MutableRocketActionSettings(id = id, type = actionType, settings = list, actions = settings)
            }
        }
    }

    companion object {
        private const val TYPE = "type"
        private const val ID = "_id"
        private const val ACTIONS = "actions"
        private fun getOrGenerateId(action: LinkedHashMap<String, Any?>): String {
            var idAsObject = action[ID]
            if (idAsObject == null || "" == idAsObject.toString()) {
                idAsObject = UUID.randomUUID().toString()
            }
            return idAsObject.toString()
        }
    }
}
