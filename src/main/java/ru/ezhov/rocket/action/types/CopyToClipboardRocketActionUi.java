package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

public class CopyToClipboardRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String TEXT = "text";

    public Component create(RocketActionSettings settings) {
        JMenuItem menuItem = new JMenuItem(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menuItem.setIcon(new ImageIcon(this.getClass().getResource("/clipboard_16x16.png")));
        menuItem.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        menuItem.addActionListener(e -> {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = defaultToolkit.getSystemClipboard();
            clipboard.setContents(new StringSelection(ConfigurationUtil.getValue(settings.settings(), TEXT)), null);
        });
        return menuItem;
    }

    @Override
    public String type() {
        return "COPY_TO_CLIPBOARD";
    }

    @Override
    public String description() {
        return "Allows you to copy a previously prepared text to the clipboard";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "Displayed title", true),
                createRocketActionProperty(DESCRIPTION, "Description that will be displayed as a hint", true),
                createRocketActionProperty(TEXT, "Text prepared for copying to the clipboard", true)
        );
    }
}
