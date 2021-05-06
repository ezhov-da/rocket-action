package ru.ezhov.quick.action;

import ru.ezhov.quick.action.contract.QuickAction;

import java.awt.Component;
import java.util.List;

public interface QuickActionRepository {
    List<Component> actions() throws QuickActionRepositoryException;
}
