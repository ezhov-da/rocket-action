package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyToClipboardQuickAction implements QuickAction {

    private String label;
    private String description;
    private String text;

    public CopyToClipboardQuickAction(String label, String description, String text) {
        this.label = label;
        this.description = description;
        this.text = text;
    }

    public Component create() {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/clipboard_16x16.png")));
        if (description != null && !"".equals(description)) {
            menuItem.setToolTipText(description);
        }
        menuItem.addActionListener(e -> {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = defaultToolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(text), null);
        });
        return menuItem;
    }
}
