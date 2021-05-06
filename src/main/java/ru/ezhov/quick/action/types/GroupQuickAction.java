package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroupQuickAction implements QuickAction {

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String COMPONENTS = "components";

    @Override
    public Component create(Map<String, Object> configuration) {
        JMenu menu = new JMenu(ConfigurationUtil.getValue(configuration, LABEL));
        menu.setIcon(new ImageIcon(this.getClass().getResource("/group_16x16.png")));
        menu.setToolTipText(ConfigurationUtil.getValue(configuration, DESCRIPTION));

        List<Component> components = (List<Component>) configuration.getOrDefault(COMPONENTS, new ArrayList<Component>());

        for (Component component : components) {
            menu.add(component);
        }

        return menu;
    }
}
