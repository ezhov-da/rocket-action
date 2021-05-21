package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import javax.swing.JSeparator;
import java.awt.Component;
import java.util.Collections;
import java.util.List;

public class SeparatorRocketActionUi extends AbstractRocketAction {

    public Component create(RocketActionSettings settings) {
        return new JSeparator();
    }

    @Override
    public String type() {
        return "SEPARATOR";
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Collections.emptyList();
    }
}
