package ru.ezhov.rocket.action.application.infrastructure

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.domain.RocketActionSettingsRepositoryException
import java.io.File
import java.net.URISyntaxException

class YmlRocketActionSettingsRepositoryTest {
    @Test
    @Throws(URISyntaxException::class, RocketActionSettingsRepositoryException::class)
    fun actions() {
        val repository = YmlRocketActionSettingsRepository(
            this.javaClass.getResource("/test-actions.yml")!!.toURI()
        )
        val actions = repository.actions()
        Assert.assertEquals(25, actions.size.toLong())
        println(actions)
    }

    @Test
    @Ignore
    @Throws(URISyntaxException::class, RocketActionSettingsRepositoryException::class)
    fun save() {
        val repository = YmlRocketActionSettingsRepository(
            File("./test.yml").toURI()
        )
        repository.save(listOf(
            object : RocketActionSettings {
                override fun id(): String = "id1"

                override fun type(): RocketActionType = RocketActionType { "test" }

                override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = mapOf(
                    RocketActionConfigurationPropertyKey("1") to "2"
                )

                override fun actions(): List<RocketActionSettings> = emptyList()
            },
            object : RocketActionSettings {
                override fun id(): String = "id2"

                override fun type(): RocketActionType = RocketActionType { "test" }

                override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = mapOf(
                    RocketActionConfigurationPropertyKey("1") to "2"
                )

                override fun actions(): List<RocketActionSettings> {
                    return listOf(
                        object : RocketActionSettings {
                            override fun id(): String = "id4"

                            override fun type(): RocketActionType = RocketActionType { "test" }

                            override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = mapOf(
                                RocketActionConfigurationPropertyKey("1") to "2"
                            )

                            override fun actions(): List<RocketActionSettings> = emptyList()
                        },
                        object : RocketActionSettings {
                            override fun id(): String = "id5"

                            override fun type(): RocketActionType = RocketActionType { "test" }

                            override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = mapOf(
                                RocketActionConfigurationPropertyKey("1") to "2"
                            )

                            override fun actions(): List<RocketActionSettings> = emptyList()
                        }
                    )
                }
            }
        ))
    }
}