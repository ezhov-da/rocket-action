package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;
import java.util.Map;

public class OpenUrlQuickAction implements QuickAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String URL = "url";

    public Component create(Map<String, Object> configuration) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(configuration, LABEL));
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));
        menuItem.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(ConfigurationUtil.getValue(configuration, URL)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return menuItem;
    }
}
