package ru.ezhov.rocket.action.application.configuration.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionSettings
import ru.ezhov.rocket.action.api.RocketActionType
import javax.swing.Icon

class TreeRocketActionSettingsTest {
    @Test
    fun test() {
        val treeRocketActionSettings = TreeRocketActionSettings(
            configuration = object : RocketActionConfiguration {
                override fun type(): RocketActionType {
                    TODO("Not yet implemented")
                }

                override fun name(): String {
                    TODO("Not yet implemented")
                }

                override fun description(): String {
                    TODO("Not yet implemented")
                }

                override fun asString(): List<RocketActionConfigurationPropertyKey> = listOf(
                    RocketActionConfigurationPropertyKey("1"),
                    RocketActionConfigurationPropertyKey("2"),
                )

                override fun properties(): List<RocketActionConfigurationProperty> {
                    TODO("Not yet implemented")
                }

                override fun icon(): Icon? = null

            },
            settings = object : RocketActionSettings {
                override fun id(): String {
                    TODO("Not yet implemented")
                }

                override fun type(): RocketActionType {
                    TODO("Not yet implemented")
                }

                override fun settings(): Map<RocketActionConfigurationPropertyKey, String> = mapOf(
                    RocketActionConfigurationPropertyKey("1") to "2",
                    RocketActionConfigurationPropertyKey("3") to "34"
                )

                override fun actions(): List<RocketActionSettings> {
                    TODO("Not yet implemented")
                }

            }
        )

        assertEquals("2", treeRocketActionSettings.asString())
    }
}
