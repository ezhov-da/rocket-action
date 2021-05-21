package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.RocketActionUiRepository;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.api.RocketActionUi;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GroupRocketActionUi extends AbstractRocketAction {

    private static final String TYPE = "GROUP";
    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";

    @Override
    public Component create(RocketActionSettings settings) {
        JMenu menu = createGroup(settings);
        RocketActionUiRepository rocketActionUiRepository = new ReflectionRocketActionUiRepository(); //TODO: сделать нормально
        createGroup(rocketActionUiRepository, settings.actions(), menu);

        return menu;
    }

    private void createGroup(RocketActionUiRepository rocketActionUiRepository, List<RocketActionSettings> actionSettings, JMenu parent) {
        for (RocketActionSettings settings : actionSettings) {
            if (settings.type().equals(TYPE)) {
                JMenu menu = createGroup(settings);
                createGroup(rocketActionUiRepository, settings.actions(), menu);
                parent.add(menu);
            } else {
                final Optional<RocketActionUi> actionUi = rocketActionUiRepository.by(settings.type());
                actionUi.ifPresent(rocketActionUi -> parent.add(rocketActionUi.create(settings)));
            }
        }
    }

    private JMenu createGroup(RocketActionSettings settings) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menu.setIcon(new ImageIcon(this.getClass().getResource("/group_16x16.png")));
        menu.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        return menu;
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String description() {
        return "description";
    }

    @Override
    public List<RocketActionConfigurationProperty> properties() {
        return Arrays.asList(
                createRocketActionProperty(LABEL, "TEST", true),
                createRocketActionProperty(DESCRIPTION, "TEST", true)
        );
    }
}
