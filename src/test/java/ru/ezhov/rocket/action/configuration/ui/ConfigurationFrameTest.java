package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;
import ru.ezhov.rocket.action.infrastructure.YmlRocketActionSettingsRepository;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

public class ConfigurationFrameTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JDialog dialog = new JDialog();
                new ConfigurationFrame(
                        dialog,
                        new ReflectionRocketActionConfigurationRepository(),
                        new ReflectionRocketActionUiRepository(),
                        new YmlRocketActionSettingsRepository(ConfigurationFrameTest.class.getResource("/actions.yml").toURI()),
                        e -> {
                        }
                ).setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}