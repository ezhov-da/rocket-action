package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.notification.NotificationFactory;
import ru.ezhov.rocket.action.notification.NotificationType;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class TextRocketActionUi extends AbstractRocketAction {
    private static String LABEL = "label";

    @Override
    public String description() {
        return "Show text";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(createRocketActionProperty(LABEL, "Text to display", true));
    }

    @Override
    public Component create(RocketActionSettings settings) {
        String text = ConfigurationUtil.getValue(settings.settings(), LABEL);
        JLabel label = new JLabel(text);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                    Clipboard clipboard = defaultToolkit.getSystemClipboard();
                    clipboard.setContents(new StringSelection(text), null);

                    NotificationFactory.getInstance().show(NotificationType.INFO, "Text copy to clipboard");
                }
            }
        });
        return label;
    }

    @Override
    public String type() {
        return "SHOW_TEXT";
    }
}
