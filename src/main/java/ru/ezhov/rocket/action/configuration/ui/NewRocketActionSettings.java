package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewRocketActionSettings implements RocketActionSettings {
    private String id;
    private String type;
    private Map<String, String> settings;
    private List<RocketActionSettings> actions;

    public NewRocketActionSettings(
            String type,
            Map<String, String> settings,
            List<RocketActionSettings> actions
    ) {
        this.type = type;
        this.settings = settings;
        this.actions = actions;
        this.id = UUID.randomUUID().toString();
    }

    @Override
    public String id() {
        return null;
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
}
