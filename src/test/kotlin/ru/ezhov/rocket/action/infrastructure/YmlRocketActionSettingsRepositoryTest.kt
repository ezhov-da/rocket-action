package ru.ezhov.rocket.action.infrastructure

import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import ru.ezhov.rocket.action.domain.RocketActionSettingsRepositoryException
import ru.ezhov.rocket.action.api.RocketActionSettings
import java.io.File
import java.net.URISyntaxException
import java.util.*

class YmlRocketActionSettingsRepositoryTest {
    @Test
    @Throws(URISyntaxException::class, RocketActionSettingsRepositoryException::class)
    fun actions() {
        val repository = YmlRocketActionSettingsRepository(
                this.javaClass.getResource("/actions.yml")!!.toURI()
        )
        val actions = repository.actions()
        Assert.assertEquals(20, actions.size.toLong())
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
                    override fun id(): String {
                        return "id1"
                    }

                    override fun type(): String {
                        return "test"
                    }

                    override fun settings(): Map<String, String> {
                        val map: MutableMap<String, String> = HashMap()
                        map["1"] = "2"
                        return map
                    }

                    override fun actions(): List<RocketActionSettings> {
                        return emptyList()
                    }
                },
                object : RocketActionSettings {
                    override fun id(): String {
                        return "id2"
                    }

                    override fun type(): String {
                        return "test"
                    }

                    override fun settings(): Map<String, String> {
                        val map: MutableMap<String, String> = HashMap()
                        map["1"] = "2"
                        return map
                    }

                    override fun actions(): List<RocketActionSettings> {
                        return listOf(
                                object : RocketActionSettings {
                                    override fun id(): String {
                                        return "id4"
                                    }

                                    override fun type(): String {
                                        return "test"
                                    }

                                    override fun settings(): Map<String, String> {
                                        val map: MutableMap<String, String> = HashMap()
                                        map["1"] = "2"
                                        return map
                                    }

                                    override fun actions(): List<RocketActionSettings> {
                                        return emptyList()
                                    }
                                },
                                object : RocketActionSettings {
                                    override fun id(): String {
                                        return "id5"
                                    }

                                    override fun type(): String {
                                        return "test"
                                    }

                                    override fun settings(): Map<String, String> {
                                        val map: MutableMap<String, String> = HashMap()
                                        map["1"] = "2"
                                        return map
                                    }

                                    override fun actions(): List<RocketActionSettings> {
                                        return emptyList()
                                    }
                                }
                        )
                    }
                }
        ))
    }
}