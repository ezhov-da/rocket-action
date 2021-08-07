package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.icon.IconService;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;

import javax.swing.JMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OpenUrlWithTextHistoryRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String BASE_URL = "baseUrl";
    private static final String ICON_URL = "iconUrl";
    private static final String PLACEHOLDER = "placeholder";

    public Component create(RocketActionSettings settings) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));

        menu.setIcon(
                IconService.load(
                        Optional.ofNullable(settings.settings().get(ICON_URL)),
                        IconRepositoryFactory.instance().by(AppIcon.LINK_INTACT)
                )
        );

        TextFieldWithText textField = new TextFieldWithText(ConfigurationUtil.getValue(settings.settings(), LABEL));
        textField.setColumns(10);

        textField.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));

        textField.addActionListener(e -> {
            String text = textField.getText();
            if (text != null && !"".equals(text)) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        URI uri = new URI(
                                ConfigurationUtil.getValue(settings.settings(), BASE_URL)
                                        .replaceAll(ConfigurationUtil.getValue(settings.settings(), PLACEHOLDER), text)
                        );
                        Desktop.getDesktop().browse(uri);
                        SwingUtilities.invokeLater(() -> {
                            Map<String, String> map = new HashMap<>();
                            map.put("label", text);
                            map.put("description", "Open link");
                            map.put("url", uri.toString());
                            menu.add(new OpenUrlRocketActionUi().create(new RocketActionSettings() {
                                @Override
                                public String id() {
                                    return null; //TODO: откорректировать
                                }

                                @Override
                                public String type() {
                                    return null; //TODO: откорректировать
                                }

                                @Override
                                public Map<String, String> settings() {
                                    return map;
                                }

                                @Override
                                public List<RocketActionSettings> actions() {
                                    return Collections.emptyList();
                                }
                            }));
                            menu.revalidate();
                            menu.repaint();
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();

                        NotificationFactory.getInstance().show(NotificationType.ERROR, "Error open URL");
                    }
                }
            }
        });

        menu.add(textField);

        return menu;
    }

    @Override
    public String type() {
        return "OPEN_URL_WITH_TEXT_HISTORY";
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
