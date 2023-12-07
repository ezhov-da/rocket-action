package ru.ezhov.rocket.action.application.api

import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service
import ru.ezhov.rocket.action.application.core.application.CreateRocketActionSettingsService

@Service
class RaPublicApi(
    private val createRocketActionSettingsService: CreateRocketActionSettingsService
) : InitializingBean {
    companion object {
        var settings: CreateRocketActionSettingsPublicApi? = null
    }

    override fun afterPropertiesSet() {
        settings = createRocketActionSettingsService
    }
}

interface CreateRocketActionSettingsPublicApi {
    fun create(groupId: String, type: String, params: Map<String, String>, tags: String? = null)
}
