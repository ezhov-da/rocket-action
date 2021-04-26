package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.QuickAction;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import java.awt.Component;
import java.util.List;

public class GroupQuickAction implements QuickAction {

    private String label;
    private String description;
    private List<QuickAction> quickActions;

    public GroupQuickAction(String label, String description, List<QuickAction> quickActions) {
        this.label = label;
        this.description = description;
        this.quickActions = quickActions;
    }

    @Override
    public ActionType type() {
        return ActionType.GROUP;
    }

    @Override
    public Component create() {
        JMenu menu = new JMenu(label);
        menu.setIcon(new ImageIcon(this.getClass().getResource("/group_16x16.png")));
        if (description != null && !"".equals(description)) {
            menu.setToolTipText(description);
        }

        for (QuickAction quickAction : quickActions) {
            menu.add(quickAction.create());
        }

        return menu;
    }
}
