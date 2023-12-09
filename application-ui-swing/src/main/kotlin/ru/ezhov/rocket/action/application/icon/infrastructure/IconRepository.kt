package ru.ezhov.rocket.action.application.icon.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Repository
import ru.ezhov.rocket.action.application.icon.domain.model.IconMetaInfo

@Repository
class IconRepository(
    objectMapper: ObjectMapper,
) : InitializingBean {
    private val cacheIcons: List<IconMetaInfo> =
        objectMapper.readValue(this::class.java.getResource("/icons/icons.json")!!)

    fun icons(): List<IconMetaInfo> = cacheIcons

    companion object {
        var INSTANCE: IconRepository? = null
    }

    override fun afterPropertiesSet() {
        INSTANCE = this
    }
}
