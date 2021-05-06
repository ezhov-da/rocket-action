package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;
import java.util.Map;

public class OpenFileQuickAction implements QuickAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String PATH = "path";

    public Component create(Map<String, Object> configuration) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(configuration, LABEL));
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/file_16x16.png")));
        menuItem.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(ConfigurationUtil.getValue(configuration, PATH)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        return menuItem;
    }
}
