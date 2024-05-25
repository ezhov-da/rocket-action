package ru.ezhov.rocket.action.plugin.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream

class YmlConfigReader(
    inputStream: InputStream
) : ConfigReader {
    private val tree: JsonNode

    init {
        val mapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModule(JavaTimeModule())

        tree = mapper.readTree(inputStream)
    }

    override fun name(): String = tree["_name"].asText()

    override fun description(): String = tree["_description"].asText()

    override fun nameBy(key: String): String = tree[key]["name"].asText()

    override fun descriptionBy(key: String): String = tree[key]["description"].asText()
}
