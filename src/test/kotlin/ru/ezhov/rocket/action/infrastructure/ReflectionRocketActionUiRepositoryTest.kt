package ru.ezhov.rocket.action.infrastructure

import org.junit.Assert
import org.junit.Test

class ReflectionRocketActionUiRepositoryTest {
    @Test
    fun test() {
        val repository = ReflectionRocketActionUiRepository()
        repository.load()
        Assert.assertEquals(17, repository.all().size.toLong())
    }
}