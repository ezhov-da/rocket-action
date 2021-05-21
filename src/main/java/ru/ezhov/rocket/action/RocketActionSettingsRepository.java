package ru.ezhov.rocket.action;

import ru.ezhov.rocket.action.api.RocketActionSettings;

import java.util.List;

public interface RocketActionSettingsRepository {
    List<RocketActionSettings> actions() throws RocketActionSettingsRepositoryException;

    void save(List<RocketActionSettings> settings) throws RocketActionSettingsRepositoryException;
}
