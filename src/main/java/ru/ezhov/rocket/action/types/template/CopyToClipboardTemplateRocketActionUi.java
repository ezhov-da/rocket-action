package ru.ezhov.rocket.action.types.template;

import ru.ezhov.rocket.action.api.RocketActionConfigurationProperty;
import ru.ezhov.rocket.action.api.RocketActionSettings;
import ru.ezhov.rocket.action.icon.IconRepositoryFactory;
import ru.ezhov.rocket.action.template.infrastructure.VelocityEngineImpl;
import ru.ezhov.rocket.action.types.AbstractRocketAction;
import ru.ezhov.rocket.action.types.ConfigurationUtil;

import javax.swing.JMenu;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;

public class CopyToClipboardTemplateRocketActionUi extends AbstractRocketAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String TEXT = "text";

    public Component create(RocketActionSettings settings) {
        String text = ConfigurationUtil.getValue(settings.settings(), TEXT);
        NotePanelEngine notePanelEngine = new NotePanelEngine(text, new VelocityEngineImpl());

        JMenu menu = new JMenu(ConfigurationUtil.getValue(settings.settings(), LABEL));
        menu.setIcon(IconRepositoryFactory.getInstance().by("clipboard-2x").get());
        menu.setToolTipText(ConfigurationUtil.getValue(settings.settings(), DESCRIPTION));
        menu.add(notePanelEngine);

        return menu;
    }

    @Override
    public String type() {
        return "COPY_TO_CLIPBOARD_TEMPLATE";
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
