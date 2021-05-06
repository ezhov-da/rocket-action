package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ShowImageQuickAction implements QuickAction {
    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String IMAGE_URL = "imageUrl";

    @Override
    public Component create(Map<String, Object> configuration) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(configuration, LABEL));
        menu.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));
        menu.setIcon(
                new ImageIcon(this.getClass().getResource("/image_16x16.png"))
        );
        try {
            JLabel label;
            if (configuration.containsKey(IMAGE_URL)) {
                label = new JLabel(new ImageIcon(new URL(ConfigurationUtil.getValue(configuration, IMAGE_URL))));
            } else {
                label = new JLabel(ConfigurationUtil.getValue(configuration, IMAGE_URL));
            }
            menu.add(new JScrollPane(label));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return menu;
    }
}
