package ru.ezhov.rocket.action.types.todoist;

import org.junit.Ignore;
import org.junit.Test;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.types.todoist.model.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;

@Ignore
public class TodoistProjectRepositoryTest {
    @Test
    public void shouldGetAllProjects() throws TodoistRepositoryException {
        TodoistProjectRepository repository = new TodoistProjectRepository();
        final List<Project> projects = repository.projects(new RocketActionSettings() {
            @Override
            public String type() {
                return null;
            }

            @Override
            public Map<String, String> settings() {
                return new HashMap<>();
            }

            @Override
            public List<RocketActionSettings> actions() {
                return null;
            }
        });

        assertFalse(projects.isEmpty());
    }
}