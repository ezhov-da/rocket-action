package ru.ezhov.quick.action;

import ru.ezhov.quick.action.types.ActionType;

import java.awt.Component;

public interface QuickAction {

    ActionType type();

    Component create();
}
