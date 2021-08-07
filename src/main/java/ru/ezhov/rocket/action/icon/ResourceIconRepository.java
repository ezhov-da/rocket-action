package ru.ezhov.rocket.action.icon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.net.URL;

public class ResourceIconRepository implements IconRepository {
    private URL defaultIcon = this.getClass().getResource("/default_16x16.png");

    @Override
    public Icon by(AppIcon icon) {
        URL url = this.getClass().getResource("/open-iconic/png/" + icon.getIconName() + ".png");
        if (url == null) {
            url = defaultIcon;
        }
        return new ImageIcon(url);
    }
}
