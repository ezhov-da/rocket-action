package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;

import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class OpenFileRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String PATH = "path";

    public Component create(RocketActionSettings settings) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menuItem.setIcon(IconRepositoryFactory.instance().by(AppIcon.FILE));
        menuItem.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(ConfigurationUtil.getValue(settings.settings(), PATH)));
                } catch (Exception ex) {
                    ex.printStackTrace();

                    NotificationFactory.getInstance().show(NotificationType.ERROR, "Error open file");
                }
            }
        });
        return menuItem;
    }

    @Override
    public String type() {
        return "OPEN_FILE";
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
                createRocketActionProperty(PATH, "TEST", true)
        );
    }
}
