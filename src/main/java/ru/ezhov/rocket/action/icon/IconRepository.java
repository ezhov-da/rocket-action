package ru.ezhov.rocket.action.icon;

import javax.swing.Icon;
import java.util.Optional;

public interface IconRepository {
    Optional<Icon> by(String name);
}
