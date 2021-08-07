package ru.ezhov.rocket.action.properties;

import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;

import java.io.IOException;
import java.util.Properties;

public class ResourceGeneralPropertiesRepository implements GeneralPropertiesRepository {
    @Override
    public Properties all() {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/general.properties"));
        } catch (IOException e) {
            e.printStackTrace();

            NotificationFactory.getInstance().show(NotificationType.ERROR, "Error read general properties");
        }
        return properties;
    }
}
