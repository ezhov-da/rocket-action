package ru.ezhov.rocket.action.application.plugin.manager.infrastructure

import org.junit.Assert
import org.junit.Test

class ReflectionRocketActionPluginRepositoryTest {
    @Test
    fun test() {
        val repository = ReflectionRocketActionPluginRepository()
        Assert.assertEquals(19, repository.all().size.toLong())
    }
}