package ru.ezhov.rocket.action.types;

import ru.ezhov.rocket.action.RocketActionUiRepository;
import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.api.RocketActionUi;
import ru.ezhov.rocket.action.icon.AppIcon;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.infrastructure.ReflectionRocketActionUiRepository;
import ru.ezhov.rocket.action.icon.IconService;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class GroupRocketActionUi extends AbstractRocketAction {

    private static final String TYPE = "GROUP";
    private static final String LABEL = "label";
    private static final String ICON_URL = "iconUrl";
    private static final String DESCRIPTION = "description";

    @Override
    public Component create(RocketActionSettings settings) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menu.setIcon(new ImageIcon(this.getClass().getResource("/load_16x16.gif")));
        menu.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));

        new GroupSwingWorker(menu, settings).execute();

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
                createRocketActionProperty(DESCRIPTION, "TEST", true),
                createRocketActionProperty(ICON_URL, "URL for icon", false)
        );
    }

    private static class GroupSwingWorker extends SwingWorker<List<Component>, String> {
        private JMenu parentMenu;
        private RocketActionSettings settings;

        public GroupSwingWorker(JMenu menu, RocketActionSettings settings) {
            this.parentMenu = menu;
            this.settings = settings;
        }

        @Override
        protected List<Component> doInBackground() throws Exception {
            RocketActionUiRepository rocketActionUiRepository = new ReflectionRocketActionUiRepository(); //TODO: сделать нормально
            return createGroup(rocketActionUiRepository, settings.actions(), parentMenu);
        }

        private List<Component> createGroup(RocketActionUiRepository rocketActionUiRepository, List<RocketActionSettings> actionSettings, JMenu parent) {
            List<Component> children = new ArrayList<>();
            for (RocketActionSettings settings : actionSettings) {
                if (settings.type().equals(TYPE)) {
                    JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
                    menu.setIcon(new ImageIcon(this.getClass().getResource("/load_16x16.gif")));
                    menu.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));

                    new GroupSwingWorker(menu, settings).execute();

                    createGroup(rocketActionUiRepository, settings.actions(), menu);
                    children.add(menu);
                } else {
                    final Optional<RocketActionUi> actionUi = rocketActionUiRepository.by(settings.type());
                    actionUi.ifPresent(rocketActionUi -> children.add(rocketActionUi.create(settings)));
                }
            }

            return children;
        }

        @Override
        protected void done() {
            try {
                final List<Component> components = this.get();
                components.forEach(c -> parentMenu.add(c));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            parentMenu.setIcon(
                    IconService.load(
                            Optional.ofNullable(settings.settings().get(ICON_URL)),
                            IconRepositoryFactory.instance().by(AppIcon.PROJECT)
                    )
            );
        }
    }
}
