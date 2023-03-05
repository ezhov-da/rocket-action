package ru.ezhov.rocket.action.application.configuration.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.api.RocketActionConfiguration
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty
import ru.ezhov.rocket.action.api.RocketActionConfigurationPropertyKey
import ru.ezhov.rocket.action.api.RocketActionType
import ru.ezhov.rocket.action.application.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.domain.model.SettingsValueType
import ru.ezhov.rocket.action.application.infrastructure.MutableRocketActionSettings
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
            settings = MutableRocketActionSettings(
                id = "123",
                type = "123",
                settings = mutableListOf(
                    SettingsModel(
                        name = "1",
                        value = "2",
                        valueType = SettingsValueType.PLAIN_TEXT,
                    ),
                    SettingsModel(
                        name = "3",
                        value = "34",
                        valueType = SettingsValueType.PLAIN_TEXT,
                    )
                )
            )
        )

        assertEquals("2", treeRocketActionSettings.asString())
    }
}
