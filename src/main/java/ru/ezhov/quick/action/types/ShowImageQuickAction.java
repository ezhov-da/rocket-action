package ru.ezhov.quick.action.types;

import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.JXPanel;
import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
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

            Component component;
            if (configuration.containsKey(IMAGE_URL)) {
                final Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension newDimension = new Dimension(dimension.width - 200, dimension.height - 200);

                JXPanel panel = new JXPanel(new BorderLayout());

                panel.setPreferredSize(newDimension);
                panel.setMaximumSize(newDimension);
                panel.setMinimumSize(newDimension);

                JXImageView imageView = new JXImageView();
                imageView.setImageURL(new URL(ConfigurationUtil.getValue(configuration, IMAGE_URL)));
                imageView.setPreferredSize(newDimension);
                imageView.setMaximumSize(newDimension);
                imageView.setMinimumSize(newDimension);

                imageView.setAutoscrolls(true);

                panel.add(
                        imageView,
                        BorderLayout.CENTER
                );

                component = imageView;
            } else {
                JPanel panel = new JPanel();
                panel.add(new JLabel(ConfigurationUtil.getValue(configuration, IMAGE_URL)));

                component = panel;
            }
            menu.add(component);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return menu;
    }
}
