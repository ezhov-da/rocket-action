package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Desktop;
import java.io.File;

public class OpenFileQuickAction implements QuickAction {

    private final String label;
    private final String description;
    private final String path;

    public OpenFileQuickAction(String label, String description, String path) {
        this.label = label;
        this.description = description;
        this.path = path;
    }

    @Override
    public ActionType type() {
        return ActionType.OPEN_FILE;
    }

    public Component create() {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/file_16x16.png")));
        if (description != null && !"".equals(description)) {
            menuItem.setToolTipText(description);
        }
        menuItem.addActionListener(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(path));
                } catch (Exception ioException) {
                }
            }
        });
        return menuItem;
    }
}
