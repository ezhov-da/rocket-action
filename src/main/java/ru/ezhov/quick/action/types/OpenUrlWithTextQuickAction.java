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
import java.util.Map;

public class OpenUrlWithTextQuickAction implements QuickAction {

    private final String LABEL = "label";
    private final String DESCRIPTION = "description";
    private final String BASE_URL = "baseUrl";
    private final String PLACEHOLDER = "placeholder";

    public Component create(Map<String, Object> configuration) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(configuration, LABEL));
        menu.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(
                new JLabel(new ImageIcon(this.getClass().getResource("/link_16x16.png")))
        );
        TextFieldWithText textField = new TextFieldWithText(ConfigurationUtil.getValue(configuration, LABEL));
        textField.setColumns(10);
        panel.add(textField);

        textField.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(
                                new URI(
                                        ConfigurationUtil.getValue(configuration, BASE_URL).replaceAll(
                                                ConfigurationUtil.getValue(configuration, PLACEHOLDER), text
                                        )
                                )
                        );
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
