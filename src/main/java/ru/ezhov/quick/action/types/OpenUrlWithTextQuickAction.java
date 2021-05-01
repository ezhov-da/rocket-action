package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;

public class OpenUrlWithTextQuickAction implements QuickAction {

    private final String label;
    private final String description;
    private final String baseUrl;
    private final String placeholder;

    public OpenUrlWithTextQuickAction(String label, String description, String baseUrl, String placeholder) {
        this.label = label;
        this.description = description;
        this.baseUrl = baseUrl;
        this.placeholder = placeholder;
    }

    public Component create() {
        JMenu menu = new JMenu(label);
        menu.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(
                new JLabel(new ImageIcon(this.getClass().getResource("/link_16x16.png")))
        );
        TextFieldWithText textField = new TextFieldWithText(label);
        textField.setColumns(10);
        panel.add(textField);

        if (description != null && !"".equals(description)) {
            textField.setToolTipText(description);
        }

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(baseUrl.replaceAll(placeholder, text)));
                    } catch (Exception ioException) {
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
