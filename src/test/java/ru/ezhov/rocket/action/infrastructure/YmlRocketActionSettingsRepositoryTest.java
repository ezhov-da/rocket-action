package ru.ezhov.rocket.action.infrastructure;

import org.junit.Ignore;
import org.junit.Test;
import ru.ezhov.rocket.action.RocketActionSettingsRepositoryException;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class YmlRocketActionSettingsRepositoryTest {

    @Test
    public void actions() throws URISyntaxException, RocketActionSettingsRepositoryException {
        YmlRocketActionSettingsRepository repository =
                new YmlRocketActionSettingsRepository(
                        this.getClass().getResource("/actions.yml").toURI()
                );

        final List<RocketActionSettings> actions = repository.actions();

        assertEquals(18, actions.size());

        System.out.println(actions);
    }

    @Test
    @Ignore
    public void save() throws URISyntaxException, RocketActionSettingsRepositoryException {
        YmlRocketActionSettingsRepository repository =
                new YmlRocketActionSettingsRepository(
                        new File("./test.yml").toURI()
                );

        repository.save(Arrays.asList(
                new RocketActionSettings() {
                    @Override
                    public String type() {
                        return "test";
                    }

                    @Override
                    public Map<String, String> settings() {
                        Map<String, String> map = new HashMap<>();
                        map.put("1", "2");
                        return map;
                    }

                    @Override
                    public List<RocketActionSettings> actions() {
                        return Collections.emptyList();
                    }
                },
                new RocketActionSettings() {
                    @Override
                    public String type() {
                        return "test";
                    }

                    @Override
                    public Map<String, String> settings() {
                        Map<String, String> map = new HashMap<>();
                        map.put("1", "2");
                        return map;
                    }

                    @Override
                    public List<RocketActionSettings> actions() {
                        return Arrays.asList(
                                new RocketActionSettings() {
                                    @Override
                                    public String type() {
                                        return "test";
                                    }

                                    @Override
                                    public Map<String, String> settings() {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("1", "2");
                                        return map;
                                    }

                                    @Override
                                    public List<RocketActionSettings> actions() {
                                        return Collections.emptyList();
                                    }
                                },
                                new RocketActionSettings() {
                                    @Override
                                    public String type() {
                                        return "test";
                                    }

                                    @Override
                                    public Map<String, String> settings() {
                                        Map<String, String> map = new HashMap<>();
                                        map.put("1", "2");
                                        return map;
                                    }

                                    @Override
                                    public List<RocketActionSettings> actions() {
                                        return Collections.emptyList();
                                    }
                                }
                        );
                    }
                }
        ));
    }
}