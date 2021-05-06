package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class OpenUrlWithTextHistoryQuickAction implements QuickAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String BASE_URL = "baseUrl";
    private static final String PLACEHOLDER = "placeholder";

    public Component create(Map<String, Object> configuration) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(configuration, LABEL));
        menu.setIcon(new ImageIcon(this.getClass().getResource("/link_16x16.png")));

        TextFieldWithText textField = new TextFieldWithText(ConfigurationUtil.getValue(configuration, LABEL));
        textField.setColumns(10);

        textField.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        URI uri = new URI(
                                ConfigurationUtil.getValue(configuration, BASE_URL)
                                        .replaceAll(ConfigurationUtil.getValue(configuration, PLACEHOLDER), text)
                        );
                        Desktop.getDesktop().browse(uri);
                        SwingUtilities.invokeLater(() -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("label", text);
                            map.put("description", "Open link");
                            map.put("url", uri.toString());
                            menu.add(new OpenUrlQuickAction().create(map));
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
