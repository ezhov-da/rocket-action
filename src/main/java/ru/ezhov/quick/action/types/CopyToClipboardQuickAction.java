package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Map;

public class CopyToClipboardQuickAction implements QuickAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String TEXT = "text";

    public Component create(Map<String, Object> configuration) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(configuration, LABEL));
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/clipboard_16x16.png")));
        menuItem.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));
        menuItem.addActionListener(e -> {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = defaultToolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(ConfigurationUtil.getValue(configuration, TEXT)), null);
        });
        return menuItem;
    }
}
