package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;

public class OpenUrlWithTextHistoryQuickAction implements QuickAction {

    private final String label;
    private final String description;
    private final String baseUrl;
    private final String placeholder;

    public OpenUrlWithTextHistoryQuickAction(String label, String description, String baseUrl, String placeholder) {
        this.label = label;
        this.description = description;
        this.baseUrl = baseUrl;
        this.placeholder = placeholder;
    }

    @Override
    public ActionType type() {
        return ActionType.OPEN_URL_WITH_TEXT;
    }

    public Component create() {
        JMenu menu = new JMenu(label);
        menu.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));

        TextFieldWithText textField = new TextFieldWithText(label);
        textField.setColumns(10);

        if (description != null && !"".equals(description)) {
            textField.setToolTipText(description);
        }

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        URI uri = new URI(baseUrl.replaceAll(placeholder, text));
                        Desktop.getDesktop().browse(uri);
                        SwingUtilities.invokeLater(() -> {
                            menu.add(new OpenUrlQuickAction(text, "Open link", uri.toString()).create());
                            menu.revalidate();
                            menu.repaint();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        menu.add(textField);

        return menu;
    }

    private static class TextFieldWithText extends JTextField {
        private final String text;

        public TextFieldWithText(String text) {
            this.text = text;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if ("".equals(this.getText())) {
                g.setColor(Color.gray);
                g.drawString(text, 5, this.getHeight() - 5);
            }
        }
    }
}
