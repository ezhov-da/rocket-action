package ru.ezhov.quick.action.configuration.ui;

import javax.swing.JFrame;

public class ConfigurationFrame {

    private JFrame frame = new JFrame("Action configuration");

    public ConfigurationFrame() {
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

    }

    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }
}
