package ru.ezhov.rocket.action.configuration.ui

import org.junit.Assert
import org.junit.Test
import ru.ezhov.rocket.action.configuration.infrastructure.ReflectionRocketActionConfigurationRepository

class ReflectionRocketActionConfigurationRepositoryTest {
    @Test
    fun shouldCreateConfigurationList() {
        val repository = ReflectionRocketActionConfigurationRepository()
        repository.load()
        val all = repository.all()
        Assert.assertFalse(all.isEmpty())
    }
}