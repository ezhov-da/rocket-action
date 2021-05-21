package ru.ezhov.rocket.action;

import ru.ezhov.rocket.action.api.RocketActionUi;

import java.util.List;
import java.util.Optional;

public interface RocketActionUiRepository {
    List<RocketActionUi> all();

    Optional<RocketActionUi> by(String type);
}
