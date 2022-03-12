package ru.ezhov.rocket.action.infrastructure

import org.junit.Assert
import org.junit.Test

class ReflectionRocketActionUiRepositoryTest {
    @Test
    fun test() {
        val repository = ReflectionRocketActionUiRepository()
        Assert.assertEquals(19, repository.all().size.toLong())
    }
}