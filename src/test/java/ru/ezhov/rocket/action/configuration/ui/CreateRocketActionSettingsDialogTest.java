package ru.ezhov.rocket.action.configuration.ui;

import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionConfigurationRepository;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class CreateRocketActionSettingsDialogTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();
            try {
                new CreateRocketActionSettingsDialog(
                        dialog,
                        new ReflectionRocketActionConfigurationRepository(),
                        new ReflectionRocketActionUiRepository()
                )
                        .show(rocketActionSettings -> {

                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}