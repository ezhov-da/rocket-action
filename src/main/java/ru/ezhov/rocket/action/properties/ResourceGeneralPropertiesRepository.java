package ru.ezhov.rocket.action.properties;

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
        }
        return properties;
    }
}
