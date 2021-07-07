package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.icon.IconService;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class OpenUrlWithTextRocketActionUi extends AbstractRocketAction {

    private final String LABEL = "label";
    private final String DESCRIPTION = "description";
    private final String BASE_URL = "baseUrl";
    private final String PLACEHOLDER = "placeholder";
    private static final String ICON_URL = "iconUrl";

    public Component create(RocketActionSettings settings) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menu.setIcon(
                IconService.load(
                        Optional.ofNullable(settings.settings().get(ICON_URL)),
                        IconRepositoryFactory.getInstance().by("link-intact-2x").get()
                )
        );

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(
                new JLabel(IconRepositoryFactory.getInstance().by("link-intact-2x").get())
        );
        TextFieldWithText textField = new TextFieldWithText(ConfigurationUtil.getValue(settings.settings(), LABEL));
        textField.setColumns(10);
        panel.add(textField);

        textField.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(
                                new URI(
                                        ConfigurationUtil.getValue(settings.settings(), BASE_URL).replaceAll(
                                                ConfigurationUtil.getValue(settings.settings(), PLACEHOLDER), text
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

    @Override
    public String type() {
        return "OPEN_URL_WITH_TEXT";
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(BASE_URL, "TEST", true),
                createRocketActionProperty(PLACEHOLDER, "TEST", true),
                createRocketActionProperty(ICON_URL, "Icon URL", false)
        );
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
