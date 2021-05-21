package ru.ezhov.rocket.action.infrastructure;

import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MutableRocketActionSettings implements RocketActionSettings {
    private String type;
    private Map<String, String> settings;
    private List<RocketActionSettings> actions;

    public MutableRocketActionSettings(
            String type,
            Map<String, String> settings,
            List<RocketActionSettings> actions
    ) {
        this.type = type;
        this.settings = settings;
        this.actions = actions;
    }

    public MutableRocketActionSettings(
            String type,
            Map<String, String> settings
    ) {
        this.type = type;
        this.settings = settings;
        this.actions = new ArrayList<>();
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Map<String, String> settings() {
        return settings;
    }

    @Override
    public List<RocketActionSettings> actions() {
        return actions;
    }

    public void add(String key, String value) {
        settings.put(key, value);
    }

    public void add(MutableRocketActionSettings settings) {
        actions.add(settings);
    }
}
