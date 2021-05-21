package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;

public class TextRocketActionUi extends AbstractRocketAction {
    private static String TEXT = "text";

    @Override
    public String description() {
        return "Show text";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(createRocketActionProperty(TEXT, "Text to display", true));
    }

    @Override
    public Component create(RocketActionSettings settings) {
        String text = ConfigurationUtil.getValue(settings.settings(), TEXT);

        return new JLabel(text);
    }

    @Override
    public String type() {
        return "SHOW_TEXT";
    }
}
