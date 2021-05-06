package ru.ezhov.quick.action.types;

import ru.ezhov.quick.action.contract.QuickAction;

import javax.swing.JSeparator;
import java.awt.Component;
import java.util.Map;

public class SeparatorQuickAction implements QuickAction {

    public Component create(Map<String, Object> configuration) {
        return new JSeparator();
    }
}
