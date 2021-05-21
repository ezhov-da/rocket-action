package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

public class OpenUrlRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String URL = "url";

    public Component create(RocketActionSettings settings) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));
        menuItem.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(ConfigurationUtil.getValue(settings.settings(), URL)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return menuItem;
    }

    @Override
    public String type() {
        return "OPEN_URL";
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(URL, "TEST", true)
        );
    }
}
