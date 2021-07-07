package ru.ezhov.rocket.action.types.template;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PanelEngine extends JPanel {
    private List<String> words;
    private Map<String, JTextField> map = new HashMap<>();

    public PanelEngine(List<String> words, KeyListener keyListenerForAllTextFields) {
        final ArrayList<String> strings = new ArrayList<>(words);

        Collections.sort(strings);

        this.words = strings;
        setLayout(new BorderLayout());

        JPanel panelTextField = new JPanel(new GridLayout(words.size(), 1));

        panelTextField.setLayout(new GridLayout(words.size(), 1));

        for (String s : this.words) {
            JTextField textField = new TextFieldWithText(s);
            map.put(s, textField);
            panelTextField.add(textField);
            if (keyListenerForAllTextFields != null) {
                textField.addKeyListener(keyListenerForAllTextFields);
            }
        }

        JLabel label = new JLabel("https://velocity.apache.org/engine/1.7/user-guide.html");
        final Color foregroundDefault = label.getForeground();
        final Cursor cursorDefault = label.getCursor();
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(label.getText()));
                } catch (Exception ioException) {
                    //no matter
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setForeground(Color.BLUE);
                label.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setForeground(foregroundDefault);
                label.setCursor(cursorDefault);
            }
        });

        add(label, BorderLayout.NORTH);
        add(panelTextField, BorderLayout.CENTER);
    }

    public Map<String, String> apply() {
        return map
                .entrySet()
                .stream()
                .collect(
                        Collectors.toMap(
                                k -> k.getKey().replaceAll("\\$", ""),
                                en -> {
                                    if ("".equals(en.getValue().getText())) {
                                        return en.getKey();
                                    } else {
                                        return en.getValue().getText();
                                    }
                                }
                        )
                );
    }

    public void initVariables(Map<String, String> map) {
        map.forEach((k, v) -> {
            final JTextField textField = this.map.get("$" + k);
            if (textField != null) {
                textField.setText(v);
            }
        });
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