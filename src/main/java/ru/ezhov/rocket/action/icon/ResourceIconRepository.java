package ru.ezhov.rocket.action.icon;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.Optional;

public class ResourceIconRepository implements IconRepository {
    @Override
    public Optional<Icon> by(String name) {
        Optional<Icon> icon;
        try {
            icon = Optional.of(
                    new ImageIcon(
                            this.getClass().getResource("/open-iconic/png/" + name + ".png")
                    )
            );
        } catch (Exception ex) {
            icon = Optional.empty();
        }
        return icon;
    }
}
