package ru.ezhov.rocket.action.application.infrastructure

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.ezhov.rocket.action.application.domain.model.ActionsModel
import ru.ezhov.rocket.action.application.domain.model.RocketActionSettingsModel
import ru.ezhov.rocket.action.application.domain.model.SettingsModel
import ru.ezhov.rocket.action.application.infrastructure.yml.YmlRocketActionSettingsRepository
import java.io.File

class YmlRocketActionSettingsRepositoryTest {
    @Test
    fun actions() {
        val repository = YmlRocketActionSettingsRepository(
            this.javaClass.getResource("/test-actions.yml")!!.toURI()
        )
        val actions = repository.actions()
        assertEquals(27, actions.actions.size)
        println(actions)
    }

    @Test
    @Disabled
    fun save() {
        val repository = YmlRocketActionSettingsRepository(
            File("./test.yml").toURI()
        )
        repository.save(
            ActionsModel(
                actions = listOf(
                    RocketActionSettingsModel(
                        id = "id1",
                        type = "test",
                        settings = listOf(
                            SettingsModel(
                                name = "1",
                                value = "2"
                            )
                        ),
                        actions = emptyList(),
                    ),
                    RocketActionSettingsModel(
                        id = "id2",
                        type = "test",
                        settings = listOf(
                            SettingsModel(
                                name = "1",
                                value = "2"
                            )
                        ),
                        actions = listOf(
                            RocketActionSettingsModel(
                                id = "id4",
                                type = "test",
                                settings = listOf(
                                    SettingsModel(
                                        name = "1",
                                        value = "2"
                                    )
                                ),
                                actions = emptyList(),
                            ),
                            RocketActionSettingsModel(
                                id = "id5",
                                type = "test",
                                settings = listOf(
                                    SettingsModel(
                                        name = "1",
                                        value = "2"
                                    )
                                ),
                                actions = emptyList(),
                            ),
                        ),
                    ),
                )
            )
        )
    }
}
