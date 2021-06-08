package ru.ezhov.rocket.action.infrastructure;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ReflectionRocketActionUiRepositoryTest {
    @Test
    public void test() {
        ReflectionRocketActionUiRepository repository = new ReflectionRocketActionUiRepository();

        assertEquals(12, repository.all().size());
    }
}