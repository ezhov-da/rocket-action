package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.types.service.IconService;

import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OpenUrlRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String URL = "url";
    private static final String ICON_URL = "iconUrl";

    public Component create(RocketActionSettings settings) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menuItem.setIcon(
                IconService.load(
                        Optional.ofNullable(settings.settings().get(ICON_URL)),
                        IconRepositoryFactory.getInstance().by("link-intact-2x").get()
                )
        );
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
                createRocketActionProperty(URL, "TEST", true),
                createRocketActionProperty(ICON_URL, "Icon URL", false)
        );
    }
}
