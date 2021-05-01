package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.net.URI;

public class OpenUrlQuickAction implements QuickAction {

    private String label;
    private String description;
    private String url;

    public OpenUrlQuickAction(String label, String description, String url) {
        this.label = label;
        this.description = description;
        this.url = url;
    }

    public Component create() {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));
        if (description != null && !"".equals(description)) {
            menuItem.setToolTipText(description);
        }
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ioException) {
                }
            }
        });
        return menuItem;
    }
}
